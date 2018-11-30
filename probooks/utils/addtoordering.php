<?php
  $servername = "localhost";
  $userdb = "root";
  $password = "";
  $dbname = "probooks";
  $username = $_POST['username'];
  $bookid = $_POST['bookid'];
  $count = $_POST['count'];
  
  // Create connection
  $conn = new mysqli($servername, $userdb, $password, $dbname);
  // Check connection
  if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
  }
  $sql = "INSERT INTO ordering(username, bookid, count) VALUES('$username', '$bookid', $count)";
  echo $sql;
  $result = $conn->query($sql);
  if ($result) {
    echo $result;
    echo "success add";
  } else {
    echo $result;
    echo "fail add";
  } 
  
  $sql2 = "SELECT LAST_INSERT_ID()";
  $result2 = $conn->query($sql2);
  if ($result2->num_rows > 0) {
    $row2 = $result2->fetch_assoc();
    echo json_encode($row2);
  } else {
    echo "nothing";
  }

?>