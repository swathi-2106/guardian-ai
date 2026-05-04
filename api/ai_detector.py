import sys
import json
from sklearn.ensemble import IsolationForest

# 🔹 Read logs from stdin
try:
    logs = json.loads(sys.stdin.read())
except:
    print(json.dumps([]))
    sys.exit(0)

# 🔹 If no logs → exit safely
if not logs:
    print(json.dumps([]))
    sys.exit(0)

# 🔹 Convert logs → features
X = []

for log in logs:
    desc = log.get("description", "").lower()
    event_type = log.get("eventType", "").lower()

    # Feature 1: message length
    length = len(desc)

    # Feature 2: suspicious keywords
    keywords = ["fail", "error", "denied", "attack", "unauthorized"]
    keyword_score = sum(1 for k in keywords if k in desc)

    # Feature 3: severity encoding
    if "error" in event_type:
        severity = 3
    elif "warning" in event_type:
        severity = 2
    else:
        severity = 1

    X.append([length, keyword_score, severity])

# 🔹 Train model
model = IsolationForest(contamination=0.05, random_state=42)
model.fit(X)

# 🔹 Predict anomalies
predictions = model.predict(X)

# 🔹 Convert anomalies → alerts
alerts = []

result = []

scores = model.decision_function(X)

for i, pred in enumerate(predictions):
    # only keep strong anomalies
    if pred == -1 and scores[i] < -0.15:
        result.append(logs[i])


print(json.dumps(result))