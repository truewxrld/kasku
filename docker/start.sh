#!/usr/bin/env bash
set -e

# Hosting platforms such as Render tell us which port to listen on.
PORT="${PORT:-80}"
sed -ri "s/Listen 80/Listen ${PORT}/" /etc/apache2/ports.conf
sed -ri "s/<VirtualHost \*:80>/<VirtualHost *:${PORT}>/" /etc/apache2/sites-available/000-default.conf

php artisan config:cache
php artisan migrate --force

exec apache2-foreground
