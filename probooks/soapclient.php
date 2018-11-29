<?php
  $client = new SoapClient("http://localhost:8888/service/transaksi?wsdl");
  $judul = $_POST['judul'];
  $params = array(
    "arg0" => $_POST['judul']
  );
  $response = $client->__soapCall("searchBook", $params);
  $books = json_encode($response);
  echo($books);
?>