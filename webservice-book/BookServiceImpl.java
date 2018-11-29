package com.probooks.jaxws.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.jws.WebService;

import java.sql.*;
import com.probooks.jaxws.beans.Book;
import com.probooks.jaxws.beans.Transaksi;
import org.json.JSONObject; 
import org.json.JSONArray;

@WebService(endpointInterface = "com.probooks.jaxws.service.BookService")  
public class BookServiceImpl implements BookService {

  @Override
  public boolean pembelian(String idbook, int quantity, String nomorPengirim) throws IOException{
		//GET BOOK DETAILS
		Book b = new Book();
		try{  
			String query = String.format("SELECT idbook, harga, kat FROM penjualan NATURAL JOIN kategori WHERE idbook='%s'", idbook);
     // Class.forName("com.mysql.cj.jdbc.Driver");  
      Connection conDB = DriverManager.getConnection(
				"jdbc:mysql://localhost:3306/book",
				"root",""
			);   
      Statement stmt = conDB.createStatement();  
      ResultSet rs = stmt.executeQuery(query);
      while(rs.next()){
				b.setId(rs.getString(1));
				b.setHarga(rs.getFloat(2));
				//b.setKategori(rs.getString(3));
			}
      conDB.close();  
    }catch(Exception e){System.out.println(e);}
	
		//SEND REQUEST TO BANK
    String USER_AGENT = "Mozilla/5.0";
    String POST_URL = "http://localhost:3000/transfer";
    String POST_PARAMS = "nomorPengirim=" + nomorPengirim + "&nomorPenerima=" + 13516999 + "&jumlah="+ quantity * b.getHarga();
    URL obj = new URL(POST_URL);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", USER_AGENT);

		// For POST only - START
		con.setDoOutput(true);
		OutputStream os = con.getOutputStream();
		os.write(POST_PARAMS.getBytes());
		os.flush();
		os.close();
		// For POST only - END

		int responseCode = con.getResponseCode();
		System.out.println("POST Response Code :: " + responseCode);

		if (responseCode == HttpURLConnection.HTTP_OK) { //success
			BufferedReader in = new BufferedReader(new InputStreamReader(
					con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			System.out.println(response.toString());
			if( response.toString().equals("true")){
				return true;
			}else{
				return false;
			}
			// masukin order yang sukses ke db book
			// // print result
			// System.out.println(response);
			// if(response.toString().equals("true")){
			// 	// Insert to DB successfull
			// 	String query ="INSERT INTO orderbook(bookid, kategori, jumlah)" + "VALUES(?, ?, ?)";
			// 	System.out.println(query);
			// 	try{  
			// 		Class.forName("com.mysql.cj.jdbc.Driver");  
			// 		Connection conDB = DriverManager.getConnection(
			// 			"jdbc:mysql://localhost:3306/bookservice",
			// 			"root",""
			// 		);   
			// 		PreparedStatement preparedStmt = conDB.prepareStatement(query);
			// 		preparedStmt.setString(1, b.getId());
			// 		preparedStmt.setString(2, b.getKategori());
			// 		preparedStmt.setFloat(3, quantity);
			// 		preparedStmt.execute();
			// 		conDB.close();  
			// 	}catch(Exception e){System.out.println(e);}				
			// }else{
				
			// }
      // return true;
		} else {
			System.out.println("POST request not worked");
      return false;
		}
  }

  @Override
	public Book[] searchBook(String term) throws IOException{
    term = term.replace(" ", "+");
    String USER_AGENT = "Mozilla/5.0";
    String GET_URL = "https://www.googleapis.com/books/v1/volumes?q=intitle:"+term;

    URL obj = new URL(GET_URL);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("User-Agent", USER_AGENT);
		int responseCode = con.getResponseCode();
		System.out.println("GET Response Code :: " + responseCode);
		StringBuffer response = new StringBuffer();
		if (responseCode == HttpURLConnection.HTTP_OK) { // success
			BufferedReader in = new BufferedReader(new InputStreamReader(
					con.getInputStream()));
			String inputLine;
			
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
		} else {
			System.out.println("GET request not worked");
		}

		JSONObject json = new JSONObject(response.toString());
	  	
  		int books_count = json.getJSONArray("items").length();
  		Book[] books = new Book[books_count];
  		
  		int kategori_count = 0;
  		for (int i = 0; i < books_count; i++) {
  			books[i] = new Book();
  			books[i].setId(json.getJSONArray("items").getJSONObject(i).getString("id"));
  			books[i].setJudul(json.getJSONArray("items").getJSONObject(i).getJSONObject("volumeInfo").getString("title"));
  			
  			//penulis
  			if (json.getJSONArray("items").getJSONObject(i).getJSONObject("volumeInfo").has("authors")) {
				JSONArray authors_array = json.getJSONArray("items").getJSONObject(i).getJSONObject("volumeInfo").getJSONArray("authors");
	  			String[] authors = new String[authors_array.length()];
	  			for (int j = 0; j < authors_array.length(); j++) {
	  				authors[j] = authors_array.getString(j);
	  			}
	  			books[i].setPenulis(authors);  				
  			} else {
  				String[] anon = new String[1];
  				anon[0] = "Anonymous";
  				books[i].setPenulis(anon);
  			}
  			
  			//deskripsi
  			if (json.getJSONArray("items").getJSONObject(i).getJSONObject("volumeInfo").has("description")) {
 				books[i].setSinopsis(json.getJSONArray("items").getJSONObject(i).getJSONObject("volumeInfo").getString("description"));
  			} else {
  				books[i].setSinopsis("No description");
  			}
  			
  			//kategori
  			
  			if (json.getJSONArray("items").getJSONObject(i).getJSONObject("volumeInfo").has("categories")) {
  				JSONArray kategori_array = json.getJSONArray("items").getJSONObject(i).getJSONObject("volumeInfo").getJSONArray("categories");
  				kategori_count = kategori_array.length();
  				String[] kategori = new String[kategori_array.length()];
	  			for (int j = 0; j < kategori_array.length(); j++) {
	  				kategori[j] = kategori_array.getString(j);
	  			}
	  			books[i].setKategori(kategori);  

  			} else {
  				kategori_count = 1;
  				String[] un_cat = new String[1];
  				un_cat[0] = "Uncategorized";
  				books[i].setKategori(un_cat);
  			}


  			//gambar
  			if (json.getJSONArray("items").getJSONObject(i).getJSONObject("volumeInfo").has("imageLinks")) {
  				books[i].setGambar(json.getJSONArray("items").getJSONObject(i).getJSONObject("volumeInfo").getJSONObject("imageLinks").getString("thumbnail"));
  			} else {
  				books[i].setGambar("public/img/default-cover.jpg");
  			}

  			//harga
			if (json.getJSONArray("items").getJSONObject(i).getJSONObject("saleInfo").has("listPrice")) {
  				books[i].setHarga(json.getJSONArray("items").getJSONObject(i).getJSONObject("saleInfo").getJSONObject("listPrice").getFloat("amount"));
  			} else {
  				books[i].setHarga(0);
  			}

  			//votes
  			if (json.getJSONArray("items").getJSONObject(i).getJSONObject("volumeInfo").has("ratingsCount")) {
  				books[i].setVotesCount(json.getJSONArray("items").getJSONObject(i).getJSONObject("volumeInfo").getInt("ratingsCount"));	
  			} else {
  				books[i].setVotesCount(0);
  			}

  			//average rating
 			if (json.getJSONArray("items").getJSONObject(i).getJSONObject("volumeInfo").has("averageRating")) {
  				books[i].setRating(json.getJSONArray("items").getJSONObject(i).getJSONObject("volumeInfo").getFloat("averageRating"));	
  			} else {
  				books[i].setRating(0);
  			}

  		}
 
  		int books_saved = 0;
  		if ((books_count % 2) == 0) { //genap
  			books_saved = books_count;
  		} else { //ganjil
  			books_saved = books_count + 1;
  		}
  		for (int i = 0; i < books_saved; i++) {  
				String query1 ="INSERT INTO kategori(idbook, kat) VALUES(?, ?)";
				System.out.println(query1);
				try{  
					//Class.forName("com.mysql.cj.jdbc.Driver");  
					Connection conDB = DriverManager.getConnection(
						"jdbc:mysql://localhost:3306/book",
						"root",""
					);   
					PreparedStatement preparedStmt = conDB.prepareStatement(query1);
					for (int j=0; j < kategori_count; j++) {
						preparedStmt.setString(1, books[i].getId());
						preparedStmt.setString(2, books[i].getKategori()[j]);
						preparedStmt.execute();
					}
					conDB.close();  
				}catch(Exception e){
					System.out.println(e);
				}	

  				String query2 ="INSERT INTO penjualan(idbook, harga, totalpenjualan) VALUES(?, ?, ?)";
				System.out.println(query2);
				try{  
					//Class.forName("com.mysql.cj.jdbc.Driver");  
					Connection conDB = DriverManager.getConnection(
						"jdbc:mysql://localhost:3306/book",
						"root",""
					);   
					PreparedStatement preparedStmt = conDB.prepareStatement(query2);
					preparedStmt.setString(1, books[i].getId());
					preparedStmt.setFloat(2, books[i].getHarga());
					preparedStmt.setInt(3, 0);
					preparedStmt.execute();
					conDB.close();  
				}catch(Exception e){
					System.out.println(e);
				}
  		}
  		return books;
  }

	@Override
	public Book getDetail(String id) throws IOException{
		String USER_AGENT = "Mozilla/5.0";
		String GET_URL = "https://www.googleapis.com/books/v1/volumes/"+id;

		URL obj = new URL(GET_URL);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("User-Agent", USER_AGENT);
		int responseCode = con.getResponseCode();
		System.out.println("GET Response Code :: " + responseCode);
		StringBuffer response = new StringBuffer();
		if (responseCode == HttpURLConnection.HTTP_OK) { // success
			BufferedReader in = new BufferedReader(new InputStreamReader(
					con.getInputStream()));
			String inputLine;
			
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

		} else {
			System.out.println("GET request not worked");
		}

		JSONObject book_json = new JSONObject(response.toString());
		Book detail = new Book();

		String judul = book_json.getJSONObject("volumeInfo").getString("title");
		String gambar;
		float rating;
		float harga;
		String sinopsis;
		
		// Penulis
		try {
			String[] penulis;
			if (book_json.getJSONObject("volumeInfo").has("authors")) {
				JSONArray authors_array = book_json.getJSONObject("volumeInfo").getJSONArray("authors");
				penulis = new String[authors_array.length()];
				for (int i = 0; i < authors_array.length(); i++) {
					penulis[i] = authors_array.getString(i);
				}
			} else {
				penulis = new String[1];
				penulis[0] = "Anonymous";
			}
			// Gambar
			if (book_json.getJSONObject("volumeInfo").has("imageLinks")) {
				if (book_json.getJSONObject("volumeInfo").getJSONObject("imageLinks").has("small")) {		
					gambar = book_json.getJSONObject("volumeInfo").getJSONObject("imageLinks").getString("small");
				} else if (book_json.getJSONObject("volumeInfo").getJSONObject("imageLinks").has("thumbnail")) {		
					gambar = book_json.getJSONObject("volumeInfo").getJSONObject("imageLinks").getString("thumbnail");
				} else {
					gambar = "public/img/default-cover.jpg";
				}
			} else {
				gambar = "public/img/default-cover.jpg";
			}

			// Harga
			if (book_json.getJSONObject("saleInfo").has("listPrice")) {
				harga = book_json.getJSONObject("saleInfo").getJSONObject("listPrice").getFloat("amount");
			} else {
				harga = 0;
			}

			// Average Rating
			if (book_json.getJSONObject("volumeInfo").has("averageRating")) {
				rating = book_json.getJSONObject("volumeInfo").getFloat("averageRating");
			} else {
				rating = 0;
			}

			// Sinopsis
			if (book_json.getJSONObject("volumeInfo").has("description")) {
				sinopsis = book_json.getJSONObject("volumeInfo").getString("description");
			} else {
				sinopsis = "No description";
			}

			// Kategori
			String[] kategori;
			if (book_json.getJSONObject("volumeInfo").has("categories")) {
  				JSONArray kategori_array = book_json.getJSONObject("volumeInfo").getJSONArray("categories");
  				kategori = new String[kategori_array.length()];
	  			for (int j = 0; j < kategori_array.length(); j++) {
	  				kategori[j] = kategori_array.getString(j);
	  			}  

  			} else {
  				kategori = new String[1];
  				kategori[0] = "Uncategorized";
  			}

			// Set Value of Detail
			detail.setId(id);
			detail.setJudul(judul);
			detail.setPenulis(penulis);
			detail.setGambar(gambar);
			detail.setRating(rating);
			detail.setHarga(harga);
			detail.setKategori(kategori);
			detail.setSinopsis(sinopsis);
		} catch (Exception e) {
			System.out.println(e);
		}
		
		return detail;
	};

	@Override
	public String getRecommendation(String kategori){
		String query = String.format("SELECT orders.orderid, orders.bookid, orders.kategori, orders.total FROM (SELECT *, sum(jumlah) total FROM orderbook WHERE kategori = '%s' GROUP BY bookid) orders WHERE orders.total = (SELECT Max(total) FROM(SELECT sum(jumlah) total FROM orderbook WHERE kategori = '%s' GROUP BY bookid) jumlahbook)" , kategori, kategori);
		// SELECT distinct idbook, totalpenjualan 
		// FROM book.penjualan natural join book.kategori 
		// WHERE kat LIKE "%Fiction%" ORDER BY totalpenjualan desc
		// LIMIT 1; 
		int orderid;
		String idbook = "0";
		int total;
		try{  
    //  Class.forName("com.mysql.cj.jdbc.Driver");  
      Connection con = DriverManager.getConnection(
				"jdbc:mysql://localhost:3306/book",
				"root",""
			);   
      Statement stmt = con.createStatement();  
      ResultSet rs = stmt.executeQuery(query);
      while(rs.next()){
				orderid = rs.getInt(1);
				idbook = rs.getString(2);
				kategori = rs.getString(3);
				total = rs.getInt(4);
				System.out.println(orderid + "  " + idbook + "  " + kategori + " " + total);
			}
      con.close();  
    }catch(Exception e){System.out.println(e);}
		return idbook;
	};
}
