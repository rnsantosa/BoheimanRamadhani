<?php
  $client = new SoapClient("http://localhost:8888/service/transaksi?wsdl");
<<<<<<< HEAD
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
=======
  $judul = $_POST['judul'];
  $params = array(
    "arg0" => $_POST['judul']
  );
  $response = $client->__soapCall("searchBook", $params);
  $books = json_encode($response);
  echo($books);
>>>>>>> 29fd3e1eb736975d472bd889a697a7097e44cd58
?>