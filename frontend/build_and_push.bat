@echo off
echo Building Docker image for kaushikshinde/coffee-frontend...
docker build -t kaushikshinde/coffee-frontend:latest .

echo.
echo Pushing image to Docker Hub...
docker push kaushikshinde/coffee-frontend:latest

echo.
echo Done!
pause
