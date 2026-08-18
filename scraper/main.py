import os
import json
import time
import random
import re
import hashlib
from datetime import datetime, timedelta
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

FB_PAGE_URLS_RAW = os.getenv("FB_PAGE_URLS", "https://m.facebook.com/BlackHoleCorporation,https://m.facebook.com/NestTutorAllBD")
TARGET_PAGES = [url.strip() for url in FB_PAGE_URLS_RAW.split(",") if url.strip()]
BACKEND_API_URL = "http://localhost:8080/api/tuitions/ingest"
BACKEND_EXISTING_IDS_URL = "http://localhost:8080/api/tuitions/existing-post-ids?days=30"

DEFAULT_PAGE_NAMES = {
    "BlackHoleCorporation": "Flash Tutors",
    "NestTutorAllBD": "Nest Tutor",
    "tutorprovide": "Tutor Provide",
    "Brighttutorsbd": "Bright Tutors"
}

try:
    PAGE_NAME_MAP = json.loads(os.getenv("FB_PAGE_NAMES", "{}"))
except Exception:
    PAGE_NAME_MAP = {}
PAGE_NAME_MAP = {**DEFAULT_PAGE_NAMES, **PAGE_NAME_MAP}

def get_page_display_name(slug_or_url):
    """Retrieve formatted display name for a page slug or URL."""
    slug = slug_or_url.rstrip("/").split("/")[-1]
    return PAGE_NAME_MAP.get(slug, slug.replace("-", " "))

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

def normalize_class_level(val):
    """Normalize classLevel to standard structured format: 'Class: <level>' or 'Class: Std-<level>'."""
    if not val:
        return None
    val = val.strip()
    # Strip any existing leading 'class\s*:\s*' or 'class\s+'
    val = re.sub(r'^class\s*[:\-]?\s*', '', val, flags=re.IGNORECASE).strip()
    # Handle 'std\s*[:\-]?\s*(\d+)'
    val = re.sub(r'^std\s*[:\-]?\s*(\d+)', r'Std-\1', val, flags=re.IGNORECASE).strip()
    if val.lower().startswith('std-'):
        val = 'Std-' + val[4:].strip()
    return f"Class: {val}"

def parse_facebook_time_to_iso(raw_time_str, ref_time=None):
    """Convert relative Facebook post time (e.g. '10m', '2h', '1d', 'Yesterday at 3:15 PM') to absolute ISO-8601 string."""
    if not ref_time:
        ref_time = datetime.now()
    if not raw_time_str or raw_time_str.lower() in ["recently", "just now"]:
        return ref_time.isoformat()
        
    raw = raw_time_str.strip().lower()
    if "t" in raw and len(raw) >= 16:
        return raw_time_str
        
    # 1. Minutes ago
    m_match = re.search(r'(\d+)\s*(?:m|min|mins|minute|minutes)\b', raw)
    if m_match:
        mins = int(m_match.group(1))
        return (ref_time - timedelta(minutes=mins)).isoformat()
        
    # 2. Hours ago
    h_match = re.search(r'(\d+)\s*(?:h|hr|hrs|hour|hours)\b', raw)
    if h_match:
        hrs = int(h_match.group(1))
        return (ref_time - timedelta(hours=hrs)).isoformat()
        
    # 3. Days ago
    d_match = re.search(r'(\d+)\s*(?:d|day|days)\b', raw)
    if d_match:
        days = int(d_match.group(1))
        return (ref_time - timedelta(days=days)).isoformat()
        
    # 4. Yesterday
    if 'yesterday' in raw:
        base = ref_time - timedelta(days=1)
        t_match = re.search(r'yesterday at (\d{1,2}):(\d{2})\s*(am|pm)?', raw)
        if t_match:
            hr, mn = int(t_match.group(1)), int(t_match.group(2))
            mer = t_match.group(3)
            if mer == 'pm' and hr < 12: hr += 12
            if mer == 'am' and hr == 12: hr = 0
            return base.replace(hour=hr, minute=mn, second=0, microsecond=0).isoformat()
        return base.isoformat()
        
    return ref_time.isoformat()

def extract_timestamp_from_container(container):
    """Extract human-readable post timestamp from Facebook post container."""
    # 1. Check permalink anchor tags with aria-label or text
    for link in container.find_all("a", href=True):
        href = link.get("href", "")
        if any(k in href for k in ["/posts/", "story_fbid", "permalink", "pfbid"]):
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
        
    return "Recently"

def extract_posts_from_all_pages():
    print(f"Launching Stealth Browser via SeleniumBase UC Mode...")
    all_extracted_posts = []
    
    with SB(uc=True, test=True, headless=True) as sb:
        for page_idx, target_url in enumerate(TARGET_PAGES):
            clean_url = target_url.replace("m.facebook.com", "www.facebook.com")
            if not clean_url.startswith("http"):
                clean_url = f"https://www.facebook.com/{clean_url}"
            
            page_slug = clean_url.rstrip("/").split("/")[-1]
            page_name = get_page_display_name(page_slug)
            print(f"\n--- Scraping Page: {page_name} ({clean_url}) ---")
            
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
                
                scroll_rounds = int(os.getenv("SCROLL_ROUNDS", "22"))
                print(f"Deep-scrolling {scroll_rounds} times with article-level DOM harvesting...")
                
                page_posts_dict = {} # Map canonical post_id -> post_dict to guarantee uniqueness
                
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
                    
                    # Live in-flight harvest of role="article" containers
                    step_soup = BeautifulSoup(sb.get_page_source(), "html.parser")
                    articles = step_soup.find_all("div", attrs={"role": "article"})
                    
                    # Fallback to feed items if role="article" isn't found
                    if not articles:
                        articles = step_soup.find_all("div", attrs={"data-ad-preview": "message"})
                        
                    for art in articles:
                        text_content = art.get_text(separator="\n", strip=True)
                        if len(text_content) < 50:
                            continue
                            
                        lower_t = text_content.lower()
                        is_tuition_related = any(k in lower_t for k in ["tuition", "tutor", "ক্লাস", "class", "ফিট", "tk", "salary", "sub-", "c-", "offer", "area", "এরিয়া", "male", "female", "req:"])
                        if not is_tuition_related:
                            continue
                            
                        # Extract canonical post ID from permalink link
                        post_id = None
                        for a in art.find_all("a", href=True):
                            href = a["href"]
                            match = re.search(r"(?:permalink/|story_fbid=|posts/|fbid=)(\d+)", href)
                            if match:
                                post_id = match.group(1)
                                break
                            pf_match = re.search(r"(pfbid[a-zA-Z0-9]+)", href)
                            if pf_match:
                                post_id = pf_match.group(1)
                                break
                                
                        # Deterministic fallback ID based on normalized content hash if no URL ID found
                        if not post_id:
                            normalized_text = re.sub(r"\s+", " ", text_content[:250]).strip()
                            post_id = f"hash_{hashlib.sha256(normalized_text.encode('utf-8')).hexdigest()[:16]}"
                            
                        posted_at_raw = extract_timestamp_from_container(art)
                        posted_at_iso = parse_facebook_time_to_iso(posted_at_raw)
                        
                        # Store in dictionary if not already captured in this session
                        if post_id not in page_posts_dict:
                            page_posts_dict[post_id] = {
                                "facebookPostId": post_id,
                                "postUrl": f"https://www.facebook.com/{page_slug}/posts/{post_id}" if not post_id.startswith("hash_") else clean_url,
                                "pageName": page_name,
                                "postedAt": posted_at_iso,
                                "postedAtRaw": posted_at_raw,
                                "rawText": text_content
                            }
                
                page_posts = list(page_posts_dict.values())
                print(f"Extracted {len(page_posts)} unique tuition posts from {page_name}!")
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
        
        CRITICAL RULES:
        1. DO NOT extract generic promotional announcement posts, teaser headlines (e.g., "20+ available tuition in Mirpur", "ইনবক্সে সিভি দিন"), or general page notices with NO concrete student offer requirements. For such posts, return NO offers (or an empty array []).
        2. Extract an item ONLY if it represents an actual, specific tuition offer or student job vacancy.
        
        For EACH valid distinct tuition offer:
        Extract all details and return a flat JSON array of objects.
        
        Each object in the returned JSON array MUST have exactly these fields:
        - "facebookPostId": (The post ID provided in the input)
        - "postUrl": (The URL provided in the input)
        - "pageName": (The page name provided in the input)
        - "postedAt": (The exact ISO timestamp provided in the input post header, e.g. "2026-08-19T02:20:00")
        - "index": (Integer sub-offer index within this post, starting at 0 for the 1st offer, 1 for the 2nd, etc.)
        - "classLevel": (CRITICAL: Format strictly as "Class: <level>" or "Class: Std-<level>", e.g., "Class: 9", "Class: 10 (English Version)", "Class: Std-4 (EM)", "Class: HSC", "Class: Play", "Class: O-Level", "Class: 6, 9")
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
        raw_desc = (offer.get("description") or "").strip()
        raw_class = offer.get("classLevel")
        raw_subject = offer.get("subject")
        raw_salary = offer.get("salary")
        
        # Discard teaser announcements or empty non-tuition items
        if not raw_class and not raw_subject and not raw_salary:
            continue
        if "available tuition" in raw_desc.lower() and not raw_subject and not raw_salary:
            continue
            
        norm_class = normalize_class_level(raw_class)
        raw_page = offer.get("pageName", "FacebookPage")
        page_name = get_page_display_name(raw_page)
        fb_id = offer.get("facebookPostId", "unknown")
        index = offer.get("index", 0)
        
        published_at = offer.get("postedAt", current_time)
        dto = {
            "compositeKey": f"{fb_id}_{index}",
            "facebookPostId": fb_id,
            "postUrl": offer.get("postUrl", ""),
            "pageName": page_name,
            "publishedAt": published_at,
            "postedAt": offer.get("postedAtRaw") or "Recently",
            "classLevel": norm_class,
            "subject": raw_subject,
            "location": offer.get("location"),
            "salary": raw_salary,
            "genderPreference": offer.get("genderPreference"),
            "description": raw_desc,
            "scrapedAt": current_time
        }
        payload.append(dto)
        
    if not payload:
        print("All extracted items were non-tuition teasers. Nothing sent to backend.")
        return
        
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
