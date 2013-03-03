
function showHideHint(btnObj, hintId) {
	var objHint = document.getElementById(hintId);
	if (objHint && btnObj) {
		var showHideFlagObj = document.getElementById('showHideFlag');
		if (showHideFlagObj) {
			if (showHideFlagObj.value == 0) {
				// hidden, we will show it
				objHint.style.display = 'block';
				btnObj.value = 'Hide hint';
				showHideFlagObj.value = 1;
			} else {
				// shown, we will hide it
				objHint.style.display = 'none';
				btnObj.value = 'Show hint';
				showHideFlagObj.value = 0;
			}
		}
	}
}

