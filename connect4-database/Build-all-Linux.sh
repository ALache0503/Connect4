#!/bin/bash

# Array mit allen Repository-Namen (ohne connect4-common)
repositories=(
    "connect4-chat"
    "connect4-database"
    "connect4-frontend"
    "connect4-gaming"
    "connect4-gateway"
    "connect4-matchmaking"
    "connect4-security"
    "connect4-statistics"
    "connect4-bot"
    "connect4-friendlist"
)

echo "Starte Docker Build für alle Repositories..."

# Baue jedes Repository nacheinander
for repo in "${repositories[@]}"; do
    echo "================================"
    echo "Baue: $repo"
    echo "================================"
    
    docker build -f "$repo/Dockerfile" -t "$repo" .
    
    if [ $? -eq 0 ]; then
        echo "✓ $repo erfolgreich gebaut"
    else
        echo "✗ Fehler beim Bauen von $repo"
        exit 1
    fi
    echo ""
done

echo "Alle Images wurden erfolgreich gebaut!"
echo ""
echo "================================"
echo "Lade RabbitMQ Image herunter..."
echo "================================"

docker pull rabbitmq

if [ $? -eq 0 ]; then
    echo "✓ RabbitMQ Image erfolgreich heruntergeladen"
else
    echo "✗ Fehler beim Herunterladen des RabbitMQ Images"
    exit 1
fi

echo ""
echo "Build-Prozess vollständig abgeschlossen!"