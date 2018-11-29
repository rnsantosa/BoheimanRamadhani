<?php
  $client = new SoapClient("http://localhost:8888/service/transaksi?wsdl");
  if($_POST['judul']){
    $params = array(
      "arg0" => $_POST['judul']
    );
    $response = $client->__soapCall("searchBook", $params);
    $books = json_encode($response);
    echo($books);
  } elseif ($_POST['id'] and $_POST['quantity'] and $_POST['nomorPengirim']) {
    $params = array(
      "arg0" => $_POST['id'],
      "arg1" => $_POST['quantity'],
      "arg2" => $_POST['nomorPengirim']
    );
    $response = $client->__soapCall("pembelian", $params);
  }
?>