import os
import time
import signal
import sys
import random
from datetime import datetime, timedelta
from dotenv import load_dotenv
import schedule
from main import run_scraper

# Load environment variables
load_dotenv(dotenv_path="../.env")

# Default interval in minutes (configurable via .env)
INTERVAL_MINUTES = int(os.getenv("SCRAPE_INTERVAL_MINUTES", "60"))
ENABLE_JITTER = os.getenv("ENABLE_JITTER", "true").lower() == "true"

running = True

def handle_exit(signum, frame):
    """Graceful exit handler on SIGINT (Ctrl+C) or SIGTERM."""
    global running
    print("\n[Scheduler] Shutdown signal received. Finishing up and exiting gracefully...")
    running = False
    sys.exit(0)

# Register signal listeners
signal.signal(signal.SIGINT, handle_exit)
signal.signal(signal.SIGTERM, handle_exit)

def execute_job():
    """Wrapper function to execute scraping job with error handling and timing."""
    start_time = datetime.now()
    print(f"\n{'='*70}")
    print(f"⏰ [Scheduler] Job Started at: {start_time.strftime('%Y-%m-%d %H:%M:%S')}")
    print(f"{'='*70}")
    
    try:
        run_scraper()
    except Exception as e:
        print(f"❌ [Scheduler Error] Exception during scraping run: {e}")
        
    duration = (datetime.now() - start_time).total_seconds()
    print(f"\n{'='*70}")
    print(f"✅ [Scheduler] Job Completed in {duration:.1f} seconds.")
    
    # Calculate next run time
    next_run = datetime.now() + timedelta(minutes=INTERVAL_MINUTES)
    if ENABLE_JITTER:
        # Add slight polite jitter (-2 to +3 minutes) so requests aren't on exact robotic intervals
        jitter_secs = random.randint(-120, 180)
        next_run = next_run + timedelta(seconds=jitter_secs)
        
    print(f"⏳ [Scheduler] Next automated run scheduled for: {next_run.strftime('%Y-%m-%d %H:%M:%S')}")
    print(f"{'='*70}\n")

def start_scheduler():
    print(f"""
============================================================
🤖 Tuition Scraper - Autonomous Background Scheduler
============================================================
  • Frequency: Every {INTERVAL_MINUTES} minutes
  • Jitter Enabled: {ENABLE_JITTER}
  • Target Interval: Hourly Facebook Page Scan
============================================================
""")
    
    # 1. Run immediately once upon startup
    print("[Scheduler] Running initial scrape on startup...")
    execute_job()
    
    # 2. Schedule recurring job
    schedule.every(INTERVAL_MINUTES).minutes.do(execute_job)
    
    # 3. Main scheduler heartbeat loop
    while running:
        schedule.run_pending()
        time.sleep(1)

if __name__ == "__main__":
    start_scheduler()
