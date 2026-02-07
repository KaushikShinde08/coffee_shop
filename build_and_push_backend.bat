@echo off
echo Building Backend Docker image for kaushikshinde/coffee-backend...
docker build -t kaushikshinde/coffee-backend:latest .

echo.
echo Pushing image to Docker Hub...
docker push kaushikshinde/coffee-backend:latest

echo.
echo Done!
pause
