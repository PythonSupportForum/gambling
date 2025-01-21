function handleStartButton() {
    console.log('Start Button clicked');
    const enableButton = document.getElementById('StartButton');
    enableButton.enabled = false;
    enableButton.style.visibility = "hidden";
    window.location.href = 'Test.html';
}