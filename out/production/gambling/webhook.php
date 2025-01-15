<?php

$secret = 'ontubs';

$signature = $_SERVER['HTTP_X_HUB_SIGNATURE_256'] ?? '';
$payload = file_get_contents('php://input');
$hash = 'sha256=' . hash_hmac('sha256', $payload, $secret);
if (!hash_equals($hash, $signature)) {
    http_response_code(403);
    die('Ungültige Signatur');
}

$repoDir = '/var/www/gambling';

$output = [];
exec("cd {$repoDir} && sudo -u www-data git pull 2>&1", $output, $returnCode);

// Loggen und antworten
if ($returnCode === 0) {
    http_response_code(200);
    echo "Git Pull erfolgreich:\n" . implode("\n", $output);
} else {
    http_response_code(500);
    echo "Fehler beim Git Pull:\n" . implode("\n", $output);
}
?>
