import os
import json
import time
import re
from datetime import datetime
from dotenv import load_dotenv
import requests
import google.generativeai as genai
from playwright.sync_api import sync_playwright
from seleniumbase import sbcdp

# Load environment variables
load_dotenv(dotenv_path="../.env")

# Configure Gemini
GEMINI_API_KEY = os.getenv("GEMINI_API_KEY")
if not GEMINI_API_KEY:
    print("WARNING: GEMINI_API_KEY is not set in .env")
genai.configure(api_key=GEMINI_API_KEY)

# Generate using Gemini 1.5 Flash
model = genai.GenerativeModel('gemini-1.5-flash')

# Comma-separated list of target Facebook Pages
FB_PAGE_URLS_RAW = os.getenv("FB_PAGE_URLS", "https://m.facebook.com/tuitioninbd/")
TARGET_PAGES = [url.strip() for url in FB_PAGE_URLS_RAW.split(",") if url.strip()]
BACKEND_API_URL = "http://localhost:8080/api/tuitions/ingest"
BACKEND_EXISTING_IDS_URL = "http://localhost:8080/api/tuitions/existing-post-ids"

def get_existing_post_ids():
    """Fetch known Facebook Post IDs from Java backend to avoid re-parsing."""
    try:
        res = requests.get(BACKEND_EXISTING_IDS_URL, timeout=5)
        if res.status_code == 200:
            existing = set(res.json())
            print(f"Fetched {len(existing)} existing post IDs from backend.")
            return existing
    except Exception as e:
        print(f"Notice: Could not fetch existing post IDs from backend ({e}). Proceeding without pre-filter.")
    return set()

def is_recent_timestamp(time_text):
    """
    Filter to ignore posts older than roughly 1 day.
    m.facebook.com formats: "Just now", "3 mins", "2 hrs", "Yesterday at 4 PM", "August 10 at 9 AM"
    """
    time_text = time_text.lower()
    if "just now" in time_text or "min" in time_text or "hr" in time_text or "yesterday" in time_text:
        return True
    return False

def extract_posts_from_all_pages():
    print(f"Launching Stealth Browser via SeleniumBase CDP...")
    sb_cdp = sbcdp.chrome(headless=True)
    endpoint_url = sb_cdp.get_endpoint_url()
    
    all_extracted_posts = []
    
    with sync_playwright() as p:
        print(f"Connecting Playwright to CDP Endpoint: {endpoint_url}")
        browser = p.chromium.connect_over_cdp(endpoint_url)
        context = browser.contexts[0] if browser.contexts else browser.new_context()
        page = context.pages[0] if context.pages else context.new_page()
        
        for target_url in TARGET_PAGES:
            # Normalize URL to mobile format
            clean_url = target_url.replace("www.facebook.com", "m.facebook.com")
            if not clean_url.startswith("http"):
                clean_url = f"https://m.facebook.com/{clean_url}"
            
            # Derive page name from URL
            page_slug = clean_url.rstrip("/").split("/")[-1]
            print(f"\n--- Scraping Page: {page_slug} ({clean_url}) ---")
            
            try:
                page.goto(clean_url, timeout=30000)
                time.sleep(4)
                
                # Scroll down to load recent posts
                for _ in range(3):
                    page.mouse.wheel(0, 1000)
                    time.sleep(2)
                
                articles = page.locator("article, div[role='article']")
                count = articles.count()
                print(f"Found {count} article blocks on {page_slug}.")
                
                for i in range(count):
                    article = articles.nth(i)
                    text_content = article.inner_text().strip()
                    
                    if not text_content or len(text_content) < 20:
                        continue
                    
                    # Look for permalink or story fbid
                    links = article.locator("a")
                    link_count = links.count()
                    post_id = f"{page_slug}_{int(time.time())}_{i}"
                    post_url = clean_url
                    
                    for j in range(link_count):
                        href = links.nth(j).get_attribute("href")
                        if href:
                            match = re.search(r"(?:permalink/|story_fbid=|posts/|fbid=)(\d+)", href)
                            if match:
                                post_id = match.group(1)
                                post_url = f"https://www.facebook.com/{page_slug}/posts/{post_id}"
                                break
                    
                    lines = text_content.split('\n')
                    is_recent = False
                    for line in lines[:5]:
                        if is_recent_timestamp(line):
                            is_recent = True
                            break
                            
                    if is_recent:
                        all_extracted_posts.append({
                            "facebookPostId": post_id,
                            "postUrl": post_url,
                            "pageName": page_slug,
                            "rawText": text_content
                        })
            except Exception as e:
                print(f"Error scraping {clean_url}: {e}")
                
        browser.close()
    
    return all_extracted_posts

def parse_with_gemini(posts_batch):
    if not posts_batch:
        return []
        
    print(f"Sending {len(posts_batch)} recent posts to Gemini 1.5 Flash for batch extraction...")
    
    prompt = """
    You are an expert data extractor. I will give you a JSON array of raw Facebook posts containing tuition offers.
    For EACH post, extract ALL tuition offers found in the text.
    Return a flat JSON array of objects.
    Each object must have exactly these keys:
    - "facebookPostId" (use the id provided in the input)
    - "postUrl" (use the url provided in the input)
    - "pageName" (use the pageName provided in the input)
    - "index" (the index of this offer within the post, 0 for the first offer, 1 for the second, etc.)
    - "classLevel" (e.g., "Class 9", "O Level")
    - "subject" (e.g., "Math, Physics")
    - "location" (e.g., "Dhanmondi")
    - "salary" (e.g., "6000 Tk")
    - "genderPreference" (e.g., "Male", "Female", "Any")
    - "description" (The raw tuition text block, or a short summary)
    
    If any field is missing, set it to null.
    ONLY return a valid JSON array, without markdown formatting or backticks.
    
    Input data:
    """ + json.dumps(posts_batch, ensure_ascii=False)
    
    try:
        response = model.generate_content(prompt)
        text = response.text.strip()
        
        # Clean up any markdown code blocks
        if text.startswith("```json"):
            text = text[7:-3]
        elif text.startswith("```"):
            text = text[3:-3]
            
        parsed_data = json.loads(text)
        return parsed_data
    except Exception as e:
        print(f"Error parsing with Gemini: {e}")
        return []

def send_to_backend(parsed_offers):
    if not parsed_offers:
        print("No valid tuition offers to send to backend.")
        return
        
    payload = []
    current_time = datetime.now().isoformat()
    
    for offer in parsed_offers:
        fb_id = offer.get("facebookPostId", "unknown")
        index = offer.get("index", 0)
        page_name = offer.get("pageName", "FacebookPage")
        
        dto = {
            "compositeKey": f"{fb_id}_{index}",
            "facebookPostId": fb_id,
            "postUrl": offer.get("postUrl", ""),
            "pageName": page_name,
            "classLevel": offer.get("classLevel"),
            "subject": offer.get("subject"),
            "location": offer.get("location"),
            "salary": offer.get("salary"),
            "genderPreference": offer.get("genderPreference"),
            "description": offer.get("description"),
            "scrapedAt": current_time
        }
        payload.append(dto)
        
    print(f"Sending {len(payload)} tuition DTOs to Java Backend...")
    try:
        response = requests.post(BACKEND_API_URL, json=payload)
        print(f"Backend response status: {response.status_code}")
        print(f"Backend response text: {response.text}")
    except Exception as e:
        print(f"Error connecting to backend: {e}")

def run_scraper():
    print("--- Starting Hourly Tuition Scraper ---")
    
    # 1. Fetch already-known post IDs from database to prevent duplicate LLM calls
    existing_ids = get_existing_post_ids()
    
    # 2. Extract recent posts from all configured Facebook pages
    recent_posts = extract_posts_from_all_pages()
    print(f"Total found {len(recent_posts)} recent posts across all pages.")
    
    # 3. Filter out posts already processed by database
    unseen_posts = [p for p in recent_posts if p["facebookPostId"] not in existing_ids]
    print(f"Filtered down to {len(unseen_posts)} completely new posts to send to Gemini.")
    
    if unseen_posts:
        parsed_offers = parse_with_gemini(unseen_posts)
        send_to_backend(parsed_offers)
    else:
        print("No new unseen posts found to process. Done!")
        
    print("--- Scraper Finished ---")

if __name__ == "__main__":
    run_scraper()
