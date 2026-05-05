#!/bin/bash
# Usage: ./scripts/switch.sh [blue|green]

set -e

NAMESPACE="pingwatch"
NEW_VERSION=$1

if [ "$NEW_VERSION" != "blue" ] && [ "$NEW_VERSION" != "green" ]; then
    echo "❌ Usage: $0 [blue|green]"
    exit 1
fi

echo "🔄 Switching traffic to: $NEW_VERSION"

# Wait for the target deployment to be ready
echo "⏳ Waiting for $NEW_VERSION to be ready..."
kubectl rollout status deployment/pingwatch-$NEW_VERSION \
    -n $NAMESPACE --timeout=120s

# Switch the service selector
kubectl patch service pingwatch-service \
    -n $NAMESPACE \
    -p "{\"spec\":{\"selector\":{\"app\":\"pingwatch\",\"version\":\"$NEW_VERSION\"}}}"

ACTIVE=$(kubectl get service pingwatch-service \
    -n $NAMESPACE \
    -o jsonpath='{.spec.selector.version}')

echo "✅ Traffic switched to: $ACTIVE"
echo "🎉 Blue-Green switch complete!"