<?php
	$fp = fsockopen("192.168.2.124", 9001, $errno, $errstr, 30);
	if (!$fp) {
		echo "$errstr ($errno)<br />\n";
	} else {
		$out = "2016-01-20 13:42:32;2016-01-20 13:46:40\n";
		fwrite($fp, $out);
		echo fgets($fp);
		fclose($fp);
	}
?>