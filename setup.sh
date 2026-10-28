#!/usr/bin/env bash
# Usage: put this file inside the extracted kasku-overlay folder and run: bash setup.sh
set -e
HERE="$(cd "$(dirname "$0")" && pwd)"
composer create-project laravel/laravel "$HERE/../kasku"
cp -r "$HERE"/. "$HERE/../kasku"/
cd "$HERE/../kasku"
rm -f setup.sh
php artisan migrate --force
php artisan db:seed --class=DemoSeeder
echo "Done. Start the server with: cd ../kasku && php artisan serve"
