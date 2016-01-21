<?php
	$fp = fsockopen("192.168.2.124", 9001, $errno, $errstr, 30);
	if (!$fp) {
		echo "$errstr ($errno)<br />\n";
	} else {
		$out = $_POST["timestamp1"].";".$_POST["timestamp2"]."\n";
		//echo $out;
		fwrite($fp, $out);
		echo fgets($fp);
		fclose($fp);
	}
?>