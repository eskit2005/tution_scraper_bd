import os
import json
import time
import random
import re
from datetime import datetime
from dotenv import load_dotenv
import requests
import google.generativeai as genai
from bs4 import BeautifulSoup
from seleniumbase import SB

# Load environment variables
load_dotenv(dotenv_path="../.env")

# Configure Gemini
GEMINI_API_KEY = os.getenv("GEMINI_API_KEY")
if not GEMINI_API_KEY:
    print("WARNING: GEMINI_API_KEY is not set in .env")
genai.configure(api_key=GEMINI_API_KEY)

# Fast, free-tier compatible models with automatic fallback
FALLBACK_MODELS = [
    "gemini-3.5-flash-lite",
    "gemini-3.1-flash-lite",
    "gemini-3.6-flash",
    "gemini-flash-lite-latest",
    "gemini-3-flash-preview"
]

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

def extract_timestamp_from_container(container):
    """Extract human-readable post timestamp from Facebook post container."""
    # 1. Check permalink anchor tags with aria-label or text
    for link in container.find_all("a", href=True):
        href = link.get("href", "")
        if any(k in href for k in ["/posts/", "story_fbid", "permalink"]):
            aria = link.get("aria-label", "").strip()
            if aria and len(aria) < 50:
                return aria
            txt = link.get_text(strip=True)
            if txt and len(txt) < 30 and (any(c.isdigit() for c in txt) or "just now" in txt.lower() or "yesterday" in txt.lower()):
                return txt
                
    # 2. Check abbr tags
    abbr = container.find("abbr")
    if abbr:
        title = abbr.get("title", "").strip()
        if title:
            return title
        txt = abbr.get_text(strip=True)
        if txt:
            return txt

    # 3. Check for relative time regex in container header text (e.g. "4m", "2h", "1d", "Yesterday at 10:00")
    header_snippet = container.get_text(separator=" ", strip=True)[:250]
    time_match = re.search(r"\b(\d+\s*(?:m|h|d|min|mins|hr|hrs|days?|hours?)\s*(?:ago)?)\b", header_snippet, re.IGNORECASE)
    if time_match:
        return time_match.group(1).strip()
        
    yesterday_match = re.search(r"\b(Yesterday at [^\n·]+)", header_snippet, re.IGNORECASE)
    if yesterday_match:
        return yesterday_match.group(1).strip()
        
    return None

def extract_posts_from_all_pages():
    print(f"Launching Stealth Browser via SeleniumBase UC Mode...")
    all_extracted_posts = []
    
    with SB(uc=True, test=True, headless=True) as sb:
        for page_idx, target_url in enumerate(TARGET_PAGES):
            clean_url = target_url.replace("m.facebook.com", "www.facebook.com")
            if not clean_url.startswith("http"):
                clean_url = f"https://www.facebook.com/{clean_url}"
            
            page_slug = clean_url.rstrip("/").split("/")[-1]
            print(f"\n--- Scraping Page: {page_slug} ({clean_url}) ---")
            
            # Add polite human-like jitter between pages if multiple pages are configured
            if page_idx > 0:
                time.sleep(random.uniform(2.5, 4.0))
            
            try:
                sb.get(clean_url)
                time.sleep(random.uniform(3.5, 4.5))
                
                # Dismiss login banner or cookie modal if present
                try:
                    sb.click("div[aria-label='Close'], div[aria-label='বন্ধ করুন'], [aria-label='Decline optional cookies']", timeout=2)
                except Exception:
                    pass
                
                # Incremental stepped scrolling with in-flight DOM harvesting
                scroll_rounds = int(os.getenv("SCROLL_ROUNDS", "22"))
                print(f"Deep-scrolling {scroll_rounds} times with live incremental DOM harvesting...")
                
                page_posts_dict = {} # Map post_id -> post_dict to preserve uniqueness
                
                for s_idx in range(scroll_rounds):
                    # Natural stepped scroll increment (900px to 1400px)
                    step = random.randint(900, 1400)
                    sb.execute_script(f"window.scrollBy(0, {step});")
                    time.sleep(random.uniform(1.8, 2.6))
                    
                    # Unfold all "See more" / "আরও দেখুন" text blocks
                    sb.execute_script("""
                        var seeMoreBtns = document.querySelectorAll('div[role="button"], span');
                        seeMoreBtns.forEach(function(btn) {
                            var txt = btn.innerText || '';
                            if (txt === 'See more' || txt === 'See More' || txt === 'আরও দেখুন') {
                                try { btn.click(); } catch(e) {}
                            }
                        });
                        var overlays = document.querySelectorAll('div[role="dialog"], div.generic_dialog, div._n3');
                        overlays.forEach(function(o) { try { o.remove(); } catch(e) {} });
                        document.body.style.overflow = 'auto';
                    """)
                    
                    # Live in-flight harvest of visible articles on this scroll step
                    step_soup = BeautifulSoup(sb.get_page_source(), "html.parser")
                    feed_messages = step_soup.find_all("div", attrs={"data-ad-preview": "message"})
                    if not feed_messages or len(feed_messages) < 2:
                        feed_messages = step_soup.find_all("div", attrs={"dir": "auto"})
                        
                    for i, m in enumerate(feed_messages):
                        text_content = m.get_text(separator="\n", strip=True)
                        if len(text_content) < 50:
                            continue
                            
                        lower_t = text_content.lower()
                        is_tuition_related = any(k in lower_t for k in ["tuition", "tutor", "ক্লাস", "class", "ফিট", "tk", "salary", "sub-", "c-", "offer", "area", "এরিয়া", "male", "female", "req:"])
                        if not is_tuition_related:
                            continue
                            
                        # Identify parent container for permalink and timestamp
                        parent = m.find_parent("div", attrs={"role": "article"}) or m.find_parent("div")
                        post_id = f"{page_slug}_step{s_idx}_{i}"
                        posted_at_val = None
                        
                        if parent:
                            posted_at_val = extract_timestamp_from_container(parent)
                            for link in parent.find_all("a", href=True):
                                href = link["href"]
                                match = re.search(r"(?:permalink/|story_fbid=|posts/|fbid=)(\d+)", href)
                                if match:
                                    post_id = match.group(1)
                                    break
                                elif "pfbid" in href:
                                    pf_match = re.search(r"(pfbid[a-zA-Z0-9]+)", href)
                                    if pf_match:
                                        post_id = pf_match.group(1)
                                        break
                                        
                        # Key by unique text fingerprint or post_id
                        text_hash = text_content[:120].strip()
                        if text_hash not in page_posts_dict and post_id not in page_posts_dict:
                            page_posts_dict[post_id] = {
                                "facebookPostId": post_id,
                                "postUrl": f"https://www.facebook.com/{page_slug}/posts/{post_id}",
                                "pageName": page_slug,
                                "postedAt": posted_at_val or "Recently",
                                "rawText": text_content
                            }
                
                page_posts = list(page_posts_dict.values())
                print(f"Extracted {len(page_posts)} full tuition posts from {page_slug}!")
                all_extracted_posts.extend(page_posts)
            except Exception as e:
                print(f"Error scraping {clean_url}: {e}")
                
    return all_extracted_posts

def parse_with_gemini(posts_batch):
    if not posts_batch:
        return []
        
    print(f"Sending {len(posts_batch)} posts to Gemini for extraction...")
    
    # Process in chunks of 6 posts max to prevent output token ceiling spikes
    chunk_size = 6
    all_parsed_offers = []
    total_chunks = (len(posts_batch) + chunk_size - 1) // chunk_size
    
    for c_idx in range(0, len(posts_batch), chunk_size):
        chunk = posts_batch[c_idx:c_idx + chunk_size]
        chunk_num = (c_idx // chunk_size) + 1
        print(f"  -> Processing chunk {chunk_num}/{total_chunks} ({len(chunk)} posts)...")
        
        prompt = """
        You are an expert tuition data extractor. I will provide a JSON array of raw Facebook posts containing tuition offers.
        Many posts contain MULTIPLE separate tuition offers (e.g. numbered with A9 76970, A6 76951, Offer 1, Offer 2, Tuition Code: 30442, etc.).
        
        For EACH distinct tuition offer found within every post:
        Extract all details and return a flat JSON array of objects.
        
        Each object in the returned JSON array MUST have exactly these fields:
        - "facebookPostId": (The post ID provided in the input)
        - "postUrl": (The URL provided in the input)
        - "pageName": (The page name provided in the input)
        - "postedAt": (The post publication timestamp or relative date provided in the input post header, e.g. "4m", "5 hours ago", "Yesterday at 3:15 PM", "14 August 2026")
        - "index": (Integer sub-offer index within this post, starting at 0 for the 1st offer, 1 for the 2nd, etc.)
        - "classLevel": (e.g., "Class 9", "Class 10", "KG (English Version)", "HSC", "Play")
        - "subject": (e.g., "General Math, Higher Math", "Physics", "All Subjects")
        - "location": (Specific area, e.g. "West Agargaon, 60 Feet, Mirpur", "Aftabnagar, Block E, Dhaka")
        - "salary": (e.g., "5000 Tk / month", "2000 Tk (5 days/week)")
        - "genderPreference": ("Male", "Female", or "Any")
        - "description": (CRITICAL: The complete, raw text block and specific requirements for THIS individual tuition offer. Do not leave blank or null.)
        
        If any specific property (like salary or gender) is not mentioned in an offer, set it to null.
        Return ONLY valid JSON array.
        
        Input data:
        """ + json.dumps(chunk, ensure_ascii=False)
        
        chunk_parsed = False
        for model_name in FALLBACK_MODELS:
            try:
                active_model = genai.GenerativeModel(model_name)
                response = active_model.generate_content(
                    prompt,
                    generation_config={"response_mime_type": "application/json"}
                )
                text = response.text.strip()
                
                if text.startswith("```json"):
                    text = text[7:-3]
                elif text.startswith("```"):
                    text = text[3:-3]
                    
                parsed = json.loads(text.strip())
                if isinstance(parsed, list):
                    print(f"     [+] Chunk {chunk_num} extracted {len(parsed)} tuition offers using {model_name}!")
                    all_parsed_offers.extend(parsed)
                    chunk_parsed = True
                    break
            except Exception as e:
                err_str = str(e)
                if "429" in err_str or "quota" in err_str.lower() or "resource" in err_str.lower():
                    print(f"     [!] Quota reached for {model_name}. Switching to next fallback model...")
                    continue
                else:
                    print(f"     [!] Error with {model_name}: {e}. Trying next model...")
                    continue
                    
        if not chunk_parsed:
            print(f"     [!] Warning: Failed to extract chunk {chunk_num} across all models.")
            
        time.sleep(2) # Polite spacing between chunks
        
    return all_parsed_offers

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
            "postedAt": offer.get("postedAt", "Recently"),
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
    
    # 3. Filter out posts already in the database
    unseen_posts = [p for p in recent_posts if p["facebookPostId"] not in existing_ids]
    print(f"Filtered down to {len(unseen_posts)} completely new posts to send to Gemini.")
    
    # 4. Parse unseen posts using Gemini (with multi-model fallback chain)
    if unseen_posts:
        parsed_offers = parse_with_gemini(unseen_posts)
        # 5. Ingest into backend
        send_to_backend(parsed_offers)
    else:
        print("All scraped posts already exist in database. Zero AI calls needed!")
        
    print("--- Scraper Finished ---")

if __name__ == "__main__":
    run_scraper()
