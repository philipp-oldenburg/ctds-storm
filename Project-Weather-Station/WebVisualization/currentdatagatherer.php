<?php
	$fp = fsockopen("192.168.2.124", 9001, $errno, $errstr, 30);
//	$fp = fsockopen("192.168.2.125", 9001, $errno, $errstr, 30);
	if (!$fp) {
		echo "$errstr ($errno)<br />\n";
	} else {
		$out = "CLASSOWM\n";
		//echo $out;
		fwrite($fp, $out);
		echo fgets($fp);
		echo ";";
		$out = "CLASSSENS\n";
		//echo $out;
		fwrite($fp, $out);
		echo fgets($fp);
		fclose($fp);
	}
?>