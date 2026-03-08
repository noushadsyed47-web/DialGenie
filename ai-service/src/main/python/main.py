from fastapi import FastAPI, HTTPException, BackgroundTasks
from pydantic import BaseModel
from typing import Optional
import logging
import os
import json
from datetime import datetime

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="DialGenie AI Service", version="1.0.0")

# Models
class CallInitRequest(BaseModel):
    call_id: str
    lead_id: str
    campaign_id: str
    phone_number: str
    lead_name: str
    concern: str
    greeting_message: Optional[str] = None
    ai_model_version: str = "v1.0"

class TranscriptRequest(BaseModel):
    call_id: str
    user_response: str
    user_emotion: Optional[str] = None

class CallSummaryRequest(BaseModel):
    call_id: str
    lead_id: str
    full_transcript: str
    call_duration: int

class CallSummaryResponse(BaseModel):
    call_id: str
    summary: str
    outcome: str  # INTERESTED, NOT_INTERESTED, CALL_BACK, NO_RESPONSE
    lead_score: int
    action_items: list
    follow_up_needed: bool
    follow_up_date: Optional[str] = None

# In-memory conversation memory (in production, use Redis)
conversation_memory = {}

@app.get("/health")
async def health_check():
    """Health check endpoint"""
    return {"status": "healthy", "service": "DialGenie AI Service"}

@app.post("/api/v1/ai/call/init")
async def initialize_call(request: CallInitRequest):
    """Initialize a call and prepare AI context"""
    logger.info(f"Initializing call {request.call_id} for lead {request.lead_id}")
    
    try:
        # Initialize conversation memory for this call
        conversation_memory[request.call_id] = {
            "call_id": request.call_id,
            "lead_id": request.lead_id,
            "lead_name": request.lead_name,
            "concern": request.concern,
            "conversation": [],
            "started_at": datetime.now().isoformat(),
            "objections": [],
            "sentiment_scores": []
        }
        
        # Generate greeting using dynamic prompting
        greeting = generate_greeting(
            request.lead_name,
            request.greeting_message,
            request.concern
        )
        
        return {
            "success": True,
            "call_id": request.call_id,
            "greeting": greeting,
            "message": "Call initialized successfully"
        }
    except Exception as e:
        logger.error(f"Error initializing call: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/v1/ai/process-response")
async def process_user_response(request: TranscriptRequest):
    """Process user speech and generate AI response"""
    logger.info(f"Processing response for call {request.call_id}")
    
    try:
        call_memory = conversation_memory.get(request.call_id)
        if not call_memory:
            raise HTTPException(status_code=404, detail="Call not found in memory")
        
        # Add user response to conversation
        call_memory["conversation"].append({
            "role": "user",
            "content": request.user_response,
            "emotion": request.user_emotion,
            "timestamp": datetime.now().isoformat()
        })
        
        # Analyze sentiment and detect objections
        sentiment_score = analyze_sentiment(request.user_response)
        call_memory["sentiment_scores"].append(sentiment_score)
        
        objection_detected = detect_objection(request.user_response)
        if objection_detected:
            call_memory["objections"].append({
                "objection": objection_detected,
                "timestamp": datetime.now().isoformat()
            })
        
        # Generate AI response using LangChain
        ai_response = generate_ai_response(
            call_memory["lead_name"],
            call_memory["concern"],
            request.user_response,
            call_memory["conversation"],
            objection_detected
        )
        
        # Add AI response to conversation
        call_memory["conversation"].append({
            "role": "assistant",
            "content": ai_response,
            "timestamp": datetime.now().isoformat()
        })
        
        # Check if we should end the call
        should_end_call = check_conversation_completion(call_memory)
        
        return {
            "success": True,
            "call_id": request.call_id,
            "ai_response": ai_response,
            "sentiment_score": sentiment_score,
            "objection_detected": objection_detected,
            "should_end_call": should_end_call
        }
    except Exception as e:
        logger.error(f"Error processing response: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/v1/ai/call-summary")
async def generate_call_summary(request: CallSummaryRequest) -> CallSummaryResponse:
    """Generate comprehensive call summary and outcome"""
    logger.info(f"Generating summary for call {request.call_id}")
    
    try:
        call_memory = conversation_memory.get(request.call_id)
        
        # Extract key information
        summary = summarize_conversation(
            request.full_transcript,
            request.call_duration
        )
        
        outcome = classify_outcome(
            request.full_transcript,
            call_memory["sentiment_scores"] if call_memory else []
        )
        
        lead_score = calculate_lead_score(
            request.full_transcript,
            outcome,
            call_memory["objections"] if call_memory else []
        )
        
        action_items = extract_action_items(request.full_transcript)
        
        follow_up_needed = outcome != "NOT_INTERESTED"
        follow_up_date = None
        if follow_up_needed:
            follow_up_date = calculate_follow_up_date(outcome).isoformat()
        
        # Clean up memory
        if request.call_id in conversation_memory:
            del conversation_memory[request.call_id]
        
        return CallSummaryResponse(
            call_id=request.call_id,
            summary=summary,
            outcome=outcome,
            lead_score=lead_score,
            action_items=action_items,
            follow_up_needed=follow_up_needed,
            follow_up_date=follow_up_date
        )
    except Exception as e:
        logger.error(f"Error generating summary: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

# AI Processing Functions (Mock implementations - replace with LangChain in production)

def generate_greeting(lead_name: str, custom_greeting: Optional[str], concern: str) -> str:
    """Generate personalized greeting using LangChain"""
    if custom_greeting:
        return f"{custom_greeting}, {lead_name}!"
    return f"Hello {lead_name}, thank you for taking my call. I'm calling regarding your concern about {concern}."

def generate_ai_response(lead_name: str, concern: str, user_input: str, conversation: list, objection: Optional[str]) -> str:
    """Generate contextual AI response using LangChain"""
    # This would use LangChain with OpenAI/Azure LLM
    # For now, returning intelligent template-based response
    if objection == "price":
        return f"{lead_name}, I understand cost is important. Let me share how this solution provides value..."
    elif objection == "timing":
        return f"{lead_name}, I get that timing might not be perfect right now. Would it help if we schedule a follow-up for next month?"
    else:
        return f"Thank you for that input. Tell me more about how we can help with your {concern}."

def analyze_sentiment(text: str) -> float:
    """Analyze sentiment of user response (-1.0 to 1.0)"""
    # Mock implementation - use TextBlob or transformers in production
    positive_words = ["good", "great", "excellent", "interested", "yes", "please"]
    negative_words = ["no", "not", "bad", "hate", "never", "stop"]
    
    positive_count = sum(1 for word in positive_words if word in text.lower())
    negative_count = sum(1 for word in negative_words if word in text.lower())
    
    if positive_count + negative_count == 0:
        return 0.0
    return (positive_count - negative_count) / (positive_count + negative_count)

def detect_objection(text: str) -> Optional[str]:
    """Detect objections in user response"""
    objections = {
        "price": ["too expensive", "cost", "price", "afford", "budget"],
        "timing": ["too soon", "later", "postpone", "next month", "not now"],
        "need": ["don't need", "don't have", "not relevant", "not applicable"],
        "competitor": ["using someone else", "competitor", "other vendor"],
        "trust": ["trust", "proven", "case studies", "testimonials"]
    }
    
    for objection_type, keywords in objections.items():
        if any(keyword in text.lower() for keyword in keywords):
            return objection_type
    
    return None

def check_conversation_completion(call_memory: dict) -> bool:
    """Check if conversation should be ended"""
    # End if we've had 100+ turns or 20+ minutes (based on estimated duration)
    turn_count = len(call_memory["conversation"])
    
    # If user explicitly asks to end or after sufficient conversation
    if turn_count > 20:
        return True
    
    # Check for explicit end signals
    last_user_response = next((m["content"] for m in reversed(call_memory["conversation"]) if m["role"] == "user"), "")
    if any(phrase in last_user_response.lower() for phrase in ["goodbye", "bye", "that's it", "thanks", "thank you"]):
        return True
    
    return False

def summarize_conversation(transcript: str, duration: int) -> str:
    """Summarize the call conversation"""
    # Mock implementation - use LangChain's summarization in production
    lines = transcript.split("\n")
    return f"Call lasted {duration} seconds with {len(lines)} conversation turns. Key topics discussed: {', '.join(lines[:3])}"

def classify_outcome(transcript: str, sentiment_scores: list) -> str:
    """Classify call outcome"""
    avg_sentiment = sum(sentiment_scores) / len(sentiment_scores) if sentiment_scores else 0
    
    if "definitely interested" in transcript.lower() or "yes, please" in transcript.lower():
        return "INTERESTED"
    elif avg_sentiment > 0.5:
        return "INTERESTED"
    elif "maybe later" in transcript.lower() or "call me back" in transcript.lower():
        return "CALL_BACK"
    elif avg_sentiment < -0.5:
        return "NOT_INTERESTED"
    else:
        return "NO_RESPONSE"

def calculate_lead_score(transcript: str, outcome: str, objections: list) -> int:
    """Calculate lead score (0-100)"""
    score = 50  # Base score
    
    if outcome == "INTERESTED":
        score += 40
    elif outcome == "CALL_BACK":
        score += 20
    elif outcome == "NOT_INTERESTED":
        score -= 30
    
    # Adjust based on objections
    score -= len(objections) * 5
    
    return max(0, min(100, score))

def extract_action_items(transcript: str) -> list:
    """Extract action items from transcript"""
    # Simple keyword matching - use NER in production
    actions = []
    if "send email" in transcript.lower():
        actions.append("Send detailed information via email")
    if "schedule" in transcript.lower() or "meeting" in transcript.lower():
        actions.append("Schedule follow-up meeting")
    if "demo" in transcript.lower():
        actions.append("Arrange product demo")
    return actions if actions else ["Follow up on proposal"]

def calculate_follow_up_date(outcome: str):
    """Calculate follow-up date based on outcome"""
    from datetime import datetime, timedelta
    
    today = datetime.now()
    if outcome == "INTERESTED":
        return today + timedelta(days=3)
    elif outcome == "CALL_BACK":
        return today + timedelta(days=7)
    else:
        return today + timedelta(days=14)

@app.on_event("shutdown")
async def shutdown_event():
    """Clean up on shutdown"""
    conversation_memory.clear()
    logger.info("AI Service shutdown")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8085)
