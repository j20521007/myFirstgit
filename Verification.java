import java.text.SimpleDateFormat;

import java.util.Scanner;

import java.net.*;
import java.io.*;
import java.util.*;
import javax.comm.*;
import java.sql.*; //引入JDBC套件

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class Verification implements Runnable, SerialPortEventListener 
{

    	static CommPortIdentifier 	portId;
    	static Enumeration	      	portList;
	static InputStream	 	inputStream;
    	Thread		      		readThread;
	static int 			numBytes;
	static int[] 			data = new int[256];
	static byte[] 			readBuffer = new byte[256];


	static SerialPort	      	serialPort;
	static OutputStream       	outputStream;
	static boolean	      		outputBufferEmptyFlag = false;
	static String  	      		defaultPort = "COM3";

    	static boolean		      	portFound = false;

	static String  address		= "http://192.168.102.106:8080/tr/test/f.jsp?";

	static String 			UID="";
	//static String 			USID_O="";
	//static String 			KEYS="";
	static String 			Block="";

	static String Userstar_PWD 	= "0123456789ABCDEF";
	static String Customer_PWD 	= "0000000000000000";
	//static String USID_O 		= "DDCCBBAAFFEEDDCCBBAA";
	static String USID_O 		= "00000000000000000000";
	//static String KEYS 		= "FFEEDDCCBBAA";
	static String KEYS 		= "000000000000";
		
	static String NewPWD 		= "";
	static String OldPWD		= "";
	static String USID		= "";
	static String PrivateState	= "";
	static String Tag_Type		= "";
	static String SetupPWD 	= "0000000000000000";
	static String PhoneNo 	= "000915305513";
	static String PWD     	= "12345678";
	static String RecNo    	=  "00";
	static String RecSt	= "00";

	static String T1		= "0123456789ABCDEF1234";
	static String C			= "";
	static String AC3		= "";
	static String All		= "";
	static String TriG		= "";
	
	static int 			retry;		
	static int[] 			Inventory = {0x04,0x60,0x00,0x00}; //讀取UID
	static int[]			LBlock = {0x05,0x63,0x00,0x00,0x00};//鎖定Block
	static int[]			RBlock = {0x05,0x61,0x00,0x00,0x00};//讀取Block
	static int[] 			WBlock = {0x09,0x62,0x00,0xAA,0xBB,0x56,0x78,0x00,0x00};//寫入Block

	//static int[]			userstarPwdOld = {0x45,0x67,0x

    	//static Connection conn	= null;
    //	static Statement stmt 		= null;
	//static ResultSet rs 		= null;
	public static boolean ChangeSample(int sampleSn)
	{
		switch(sampleSn)
		{
			//case 0:
			//	T1 = "AA7654BB1098765432FF"; //T1
			//	WriteMultiBlock("00000001",25);

			//	break;

			case 1:
				T1 = "AA7655321AB876543210"; //T1
				USID 		= "DDCCBBAAFFEEDDCCBBAA";
				KEYS 		= "FFEEDDCCBBAA";
				break;
			case 2:
				T1 = "BB76543210CC76543210"; //T1
				USID 		= "0123456789ABCDEF1234";
				KEYS 		= "123456BBCDA3";
				break;

			case 3:
				T1 = "EF765432109876544210"; //T1
				USID 		= "AAAAAAAAAAAAAAAAAAAA";
				KEYS 		= "EFEAC123BCEF";
				break;
			case 4:
				T1 		= "977654321CE876543210"; //T1
				USID 		= "876123123ABCD2313EFA";
				KEYS 		= "678923FBAE72";
				break;
			case 5:
				T1 		= "3176543EA10987654321"; //T1
				USID 		= "AC1234FEDCAA23322BBA";
				KEYS 		= "ABCE1289901C";
				break;
			default:
				USID 		= "00000000000000000000";
				KEYS 		= "000000000000";	
				break;
		}
		System.out.println("Switch USID="+USID+",KEY="+KEYS);
		///String usid = null;
		//String key = null;
		if(Userstar_PWD.equals(WriteMultiBlock(Userstar_PWD,18)) && USID.equals(WriteMultiBlock(USID,20)) && WriteCommand(0x35) == 0x30)
		{

			if(Customer_PWD.equals(WriteMultiBlock(Customer_PWD,18)) && KEYS.equals(WriteMultiBlock(KEYS,20)) && WriteCommand(0x20) == 0x30)
			{
				return upload();
			}
			else
			{
				return false;
			}
		}
		else
		{
			return false;
		}		
	}

	public static String getTime()
	{
		java.util.Date now = new java.util.Date(); //取得現在時間
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sf.setTimeZone(TimeZone.getTimeZone("GMT")); // 設定時區為格林威治 GMT 時區
		String sGMT = sf.format(now);
		return sGMT;
	}

  	public static Connection getConnection() throws Exception 
	{
    		// Load the JDBC driver
    		String driver = "com.mysql.jdbc.Driver";
    		//String driver = "org.gjt.mm.mysql.Driver";
    		Class.forName(driver);

    		// Create a connection to the database
    		String url = "jdbc:mysql://localhost:3306/CHIPDB?userUnicode=true";
    		String username = "root";
    		String password = "2398788";
    		return DriverManager.getConnection(url, username, password);
  	}

	public static boolean ReadUID2DB(int sn,String uid)
	{
    		Connection conn = null;
   		Statement stmt = null;
   		ResultSet rs = null;
   		int a = 1;
   		try 
		{
    			conn = getConnection();
    			stmt = conn.createStatement();
    	 		String query = "SET NAMES 'utf8'";
     			rs = stmt.executeQuery(query);
			//query = "insert into  `CHIPDB`.`chip`(`uid`, `usid`, `keys`, `uploadTime`) VALUES ('88888', '213123', '213123', '');";
			query = "INSERT INTO `CHIPDB`.`ReadUID` (`sn`, `uid`, `uploadTime`) VALUES ('"+sn+"', '"+uid+"', '"+getTime()+"');";

    			a = stmt.executeUpdate(query);

      			//query = "select * from chip";
      			//rs = stmt.executeQuery(query);
      			//while (rs.next())
			//{
        		//	System.out.println(rs.getString("uid") + " " + rs.getString("usid") + " "+ rs.getString("keys"));
      			//}
    		} 
		catch (Exception e) 
		{
      			// handle the exception
			e.printStackTrace();
			System.err.println(e.getMessage());
    		}
		return true;
	}

	public static boolean WriteUSID2DB(int sn,String usidWrite,String usidRead,String RSCode)
	{
    		Connection conn = null;
   		Statement stmt = null;
   		ResultSet rs = null;
   		int a = 1;
   		try 
		{
    			conn = getConnection();
    			stmt = conn.createStatement();
    	 		String query = "SET NAMES 'utf8'";
     			rs = stmt.executeQuery(query);
			//query = "insert into  `CHIPDB`.`chip`(`uid`, `usid`, `keys`, `uploadTime`) VALUES ('88888', '213123', '213123', '');";
			query = "INSERT INTO `CHIPDB`.`WriteUSID` (`sn`, `usidWrite`,`usidRead`,`rscode`, `uploadTime`) VALUES ('"+sn+"', '"+usidWrite+"', '"+usidRead+"','"+RSCode+"','"+getTime()+"');";

    			a = stmt.executeUpdate(query);

   		} 
		catch (Exception e) 
		{
      			// handle the exception
			e.printStackTrace();
			System.err.println(e.getMessage());
    		}
		return true;
	}
	
	public static int WriteCommand(int command)
	{
		//寫入Command Code
		WBlock[2]=25;
		WBlock[3]=0x00;  	//Byte 3 of Block 25
		WBlock[4]=0x00;
		WBlock[5]=0x00;
		WBlock[6]=command;	//Byte 0 of Block 25
		writeRs232(WBlock,WBlock[0]);
		
		//讀出Response Code
		RBlock[2]=25;
		writeRs232(RBlock,RBlock[0]);
		return HexStr2Int(Block.substring(6,8));
	}

	public static String ReadMultiBlock(int shift,int Length)
	{
		String data = "";
		int mod = Length % 8;
		int blocks = 0;
		int i ;
		if(mod != 0) 
		{
			blocks = (Length + (8 - mod) ) / 8;
		}
		else
		{
			blocks = Length /8;
		}

		//讀出Data
		for(i=0;i < blocks;i++)
		{
			RBlock[2]=shift+i;
			writeRs232(RBlock,RBlock[0]);
			data = ReverseData(Block) + data;
		}

		//踢除補0的部分,取回真正的Data
		data = data.substring(data.length()-Length,data.length());

		//傳回Data作為比對用
		return data;
	}

	public static String WriteMultiBlock(String Data,int shift)
	{
		String data = Data;

		//檢查是否為合法HEX字串
		if(! HexStrCheck(data)) return null;

		int mod = data.length() % 8;
		int deta = (8 - mod);

		int i,j;
		
		//若不?一個Block則進行補0
		if(mod != 0) for(i=0;i < deta;i++) data = "0" + data;
		int blocks = data.length() / 8;

		data =  ReverseData(data);

		//反轉之後仍產生null字串則回傳null并?止
		if(data == null) return null;

		//寫入Data
		for(i=0;i < blocks;i++) //?入Block數
		{
			WBlock[2]=shift+i;

			for(j=0;j<4;j++)  //填入Byte 3~0;先填代表是高位元?
			{
				WBlock[3+j]=HexStr2Int(data.substring(8*i+2*(4-j)-2,8*i+2*(4-j)));
			}
			writeRs232(WBlock,WBlock[0]);
		}

		data = "";

		//讀出Data
		for(i=0;i < blocks;i++)
		{
			RBlock[2]=shift+i;
			writeRs232(RBlock,RBlock[0]);
			//data = ReverseBlock(Block) + data;
			data = ReverseData(Block) + data;

		}

		//踢除補0的部分,取回真正的Data
		data = data.substring(data.length()-Data.length(),data.length());

		//傳回Data作為比對用
		return data;
		
	}

	public static String ReverseData(String Data)
	{
		if((Data.length() % 2)==0 && HexStrCheck(Data))
		{
			int i;
			String temp = "";
			for(i=0;i<(Data.length()/2);i++) temp = Data.substring(2*i,2*(i+1)) + temp ;
			return temp;
		}
		else
		{
			return null;
		}
	}

	public static int HexStr2Int(String hexStr)
	{
		if(hexStr.length()==2)
		{
			int a,b;
			a = Character.digit(hexStr.charAt(0),16);
			b = Character.digit(hexStr.charAt(1),16);
			if(a>=0 && b>=0)
			{
				return 16*a + b;
			}
			else
			{			
				return -1;
			}
		}
		else
		{
			return -1;
		}
	}

	public static boolean HexStrCheck(String hexStr)
	{
		int i,j;
		j=0;
		for(i=0;i < hexStr.length();i++)
		{
			if(Character.getNumericValue(hexStr.charAt(i)) > 15 || Character.getNumericValue(hexStr.charAt(i)) < 0) j++;
		}

		if(j==0)
		{
			return true;
		}
		else
		{
			return false;
		}
	}


	public static String HextoString(int[] Hex,int offset,int len)
	{
		String Buffer = "";
		int i;
		for(i=(offset+len-1);i >= offset;i--)
		{
			Buffer = Buffer + Integer.toHexString((Hex[i] >> 4) & 0x0f);
			Buffer = Buffer + Integer.toHexString(Hex[i] & 0x0f);
		}
		return Buffer.toUpperCase();
	}


	public static boolean uploadAC3()
	{
		if(AC3.length()==0||T1.length()==0||C.length()==0)
		{
			System.out.println("AC3/T1/C is null");
			return false;
		}

		String Addr ;

		Addr = "http://192.168.102.106:8080/tr/test/u.jsp?uid=" + UID + "&c="+ C + "&t1=" + T1+"&ac3="+AC3;

		System.out.println(Addr);	

	       	try
       		{   	// 建立URL物件
           		URL url = new URL(Addr);

			HttpURLConnection.setFollowRedirects(false);
			HttpURLConnection http = (HttpURLConnection) url.openConnection();
			http.setRequestMethod("HEAD");

			//自訂ResponseCode in WEB Server
			//Server端:response.setStatus(200) in JSP \\ response.status = "200 Upload OK" in ASPX
			//http://skenyeh.blogspot.com/2011/01/http-status-codes.html
			System.out.println(http.getResponseCode());

			if(http.getResponseCode() == 200)  
			{
				return true;
			}
			else
			{	
				return false;
			}
      		}
      		catch(Exception e) 
      		{
			return false;
       		}
	}

	
	public static boolean upload()
	{
		if(UID.length()==0||USID_O.length()==0||KEYS.length()==0)
		{
			System.out.println("UID/USID_O/KEYS is null");
			return false;
		}

		String Addr ;

		Addr = address + "uid=" + UID + "&usid="+ USID + "&k=" + KEYS;

		System.out.println(Addr);	

	       	try
       		{   	// 建立URL物件
           		URL url = new URL(Addr);

			HttpURLConnection.setFollowRedirects(false);
			HttpURLConnection http = (HttpURLConnection) url.openConnection();
			http.setRequestMethod("HEAD");

			//自訂ResponseCode in WEB Server
			//Server端:response.setStatus(200) in JSP \\ response.status = "200 Upload OK" in ASPX
			//http://skenyeh.blogspot.com/2011/01/http-status-codes.html
			System.out.println(http.getResponseCode());

			if(http.getResponseCode() == 200)  
			{
				return true;
			}
			else
			{	
				return false;
			}
      		}
      		catch(Exception e) 
      		{
			return false;
       		}
	}


    	public static void main(String[] args) 
	{

		//String UID 		= "";

 		if (args.length > 0) 
		{
	    		defaultPort = args[0];
		} 
   
		portList = CommPortIdentifier.getPortIdentifiers();

		while (portList.hasMoreElements()) 
		{
	    		portId = (CommPortIdentifier) portList.nextElement();
				//System.out.println("Found port: "+portId.getName());
	    		if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) 
			{
				if (portId.getName().equals(defaultPort)) 
				{
		    			System.out.println("Found port: "+defaultPort);
		    			portFound = true;
		    			Verification reader = new Verification();
					
				} 
	    		} 
		} 

		if (!portFound) 
		{
	    		System.out.println("port " + defaultPort + " not found.");
		} 
		retry = 0;
		while(true)
		{	System.out.println("");
			System.out.println("+----------------------------------------+");
			System.out.println("|Verification system for FPGA Func V1    |");
			System.out.println("+----------------------------------------+");
			System.out.println("| 0 --> Upload UID,USID_O,KEYS           |");
			System.out.println("| 1 --> UID Read                         |");
			System.out.println("| 2 --> Block Read                       |");
			System.out.println("| 3 --> Block Write                      |");
			System.out.println("| 4 --> Blanking All Blocks              |");
			System.out.println("| 5 --> Block Lock                       |");
			System.out.println("|21 --> 0x21 Update Customer_PWD         |");
			System.out.println("|20 --> 0x20 Write KEYS                  |");
			System.out.println("|33 --> 0x33 Write Private_state         |");
			System.out.println("|31 --> 0x31 Update Userstar_PWD         |");
			System.out.println("|35 --> 0x35 Write USID_O                |");
			System.out.println("|34 --> 0x34 Write Tag_Type              |");

			System.out.println("|358 --> 0x35 Write USID_O               |");
			System.out.println("|208 --> 0x20 Write KEYS                 |");

			System.out.println("|51 --> Test Sample 1                    |");
			System.out.println("|52 --> Test Sample 2                    |");
			System.out.println("|53 --> Test Sample 3                    |");
			System.out.println("|54 --> Test Sample 4                    |");
			System.out.println("|55 --> Test Sample 5                    |");
			System.out.println("|66 --> Upload to Local Host             |");
			System.out.println("|77 --> Upload to Ming's Server          |");
			System.out.println("|99 --> EXIT                             |");
			System.out.println("+----------------------------------------+");
			System.out.println("|Programmer:Gershwin,Hsu@2011/3/20 12:30 |");
			System.out.println("+----------------------------------------+");

			Scanner scanner = new Scanner(System.in);
			
			switch(scanner.nextInt())
			{
				case 500:
					WriteMultiBlock("AAAAAAAAAAAAAAAA",18);
					WriteMultiBlock("00000007",20);
					System.out.println("Result of Change Private_state "+WriteCommand(0x33));
					break;

				case 60:
					WriteMultiBlock(T1,18);
					
					System.out.println("Result of T1 Write is "+WriteCommand(0x01));
					System.out.println("AC3 generate result is "+Integer.toString(WriteCommand(0x02)));
					AC3 = ReadMultiBlock(18,20);
					C = ReadMultiBlock(26,8);

					System.out.println("T1 is "+T1);
					System.out.println("C is "+C);
					System.out.println("AC3 is "+ReadMultiBlock(18,20));
					
					//System.out.println("Result of upload ac3 is "+uploadAC3());
					break;			
				case 200:
				
					int usidOK = 0;
					int kOK = 0;
					int tOK = 0;
					writeRs232(Inventory,Inventory[0]);


					//T1 = "AA765432109876543210"; //T1
					writeRs232(Inventory,Inventory[0]);

					WriteMultiBlock("BBBBBBBBBBBBBBBB",18);
					WriteMultiBlock(USID,20);
					usidOK = WriteCommand(0x35);

					WriteMultiBlock("AAAAAAAAAAAAAAAA",18);
					WriteMultiBlock(KEYS,20);
					kOK = WriteCommand(0x20);
					System.out.println("Upload USID,KEY of Tag "+UID+upload());
					WriteMultiBlock(T1,18);
					tOK = WriteCommand(0x01);

					if((usidOK == 0x30) && (kOK == 0x30) && (tOK == 0x00))
					{
						System.out.println("AC3 generate result is "+Integer.toString(WriteCommand(0x02)));
						AC3 = ReadMultiBlock(18,20);
						C = ReadMultiBlock(26,8);

						System.out.println("C is "+C);
						System.out.println("AC3 is "+ReadMultiBlock(18,20));
						System.out.println("Result of upload ac3 is "+uploadAC3());
					}
					else
					{
						System.out.println("Write USID="+usidOK+"/KEY="+kOK+"/T1="+tOK);
					}

					
//					System.out.println("Result of upload ac3 is "+uploadAC3());
					break;

				case 51:
					System.out.println("Rsult of Change sample 1 and upload is "+ChangeSample(1));
					break;

				case 52:
					System.out.println("Rsult of Change sample 2 and upload is "+ChangeSample(2));
					break;

				case 53:
					System.out.println("Rsult of Change sample 3 and upload is "+ChangeSample(3));
					break;

				case 54:
					System.out.println("Rsult of Change sample 4 and upload is "+ChangeSample(4));
					break;

				case 55:
					System.out.println("Rsult of Change sample 5 and upload is "+ChangeSample(5));
					break;

				
				case 66:
					address = "http://192.168.102.233/chip/upload.aspx?";
					System.out.println("Upload url is " + address);
					break;
				case 77:
					address = "http://192.168.102.106:8080/tr/test/f.jsp?";;
					System.out.println("Upload url is " + address);
					break;

				case 20:
				   System.out.println("========== 0x20 Write KEY =====================");
					/*for(retry=1;retry<4;retry++)
					{
						switch(retry)
						{
							case 1:
								OldPWD = "9999999999999999";
								KEYS   = "111111111111";
								break;
							case 2:
								OldPWD = "AAAAAAAAAAAAAAAA";
								KEYS   = "000000000000";
								break;
							case 3:
								OldPWD = "AAAAAAAAAAAAAAAA";
								KEYS   = "111111111111";
								break;
							default:
								break;
						}
						if(OldPWD.equals(WriteMultiBlock(OldPWD,18)) && KEYS.equals(WriteMultiBlock(KEYS,20)))
						{
							System.out.println("Round[20"+retry+"]/0x20/Customer_PWD="+OldPWD+"/KEY="+KEYS+"/RSCODE=0x"+Integer.toHexString(WriteCommand(0x20)));
						}
						else
						{
							System.out.println("Not OK");
						}
					}*/
						Customer_PWD = "AAAAAAAAAAAAAAAA";
						//KEYS   = "BB1234567890";
				//USID 		= "DDCCBBAAFFEEDDCCBBAA";
				KEYS 		= "123456ABCDEF";
		if(Customer_PWD.equals(WriteMultiBlock(Customer_PWD,18)) && KEYS.equals(WriteMultiBlock(KEYS,20)))
						{
							System.out.println("Round[20"+retry+"]/0x35/Customer_PWD="+Customer_PWD+"/KEY="+KEYS+"/RSCODE=0x"+Integer.toHexString(WriteCommand(0x20)));
						}
						else
						{
							System.out.println("Not OK");
						}
					break;

				case 35:
				   System.out.println("========== 0x35 Write USID =====================");
/*					for(retry=1;retry<4;retry++)
					{
						switch(retry)
						{
							case 1:
								OldPWD = "9999999999999999";
								USID   = "11111111111111111111";
								break;
							case 2:
								OldPWD = "BBBBBBBBBBBBBBBB";
								USID   = "00000000000000000000";
								break;
							case 3:
								OldPWD = "BBBBBBBBBBBBBBBB";
								USID   = "11111111111111111111";
								break;
							default:
								break;
						}

						if(OldPWD.equals(WriteMultiBlock(OldPWD,18)) && USID.equals(WriteMultiBlock(USID,20)))
						{
							System.out.println("Round[20"+retry+"]/0x35/Userstar_PWD="+OldPWD+"/USID="+USID+"/RSCODE=0x"+Integer.toHexString(WriteCommand(0x35)));
						}
						else
						{
							System.out.println("Not OK");
						}
					}
*/
						Userstar_PWD = "BBBBBBBBBBBBBBBB";
						USID   = "1234567890ABCDEF1234";
				//USID 		= "DDCCBBAAFFEEDDCCBBAA";
				//KEYS 		= "FFEEDDCCBBAA";
		if(Userstar_PWD.equals(WriteMultiBlock(Userstar_PWD,18)) && USID.equals(WriteMultiBlock(USID,20)))
						{
							System.out.println("Round[20"+retry+"]/0x35/Userstar_PWD="+Userstar_PWD+"/USID="+USID+"/RSCODE=0x"+Integer.toHexString(WriteCommand(0x35)));
						}
						else
						{
							System.out.println("Not OK");
						}
					break;
				

				case 33:
				   System.out.println("========== 0x33 Private_state =====================");
					 
					for(retry=0;retry<0xff;retry++)
					{
						switch(retry)
						{
							case 0:
								System.out.print("\nTEST CASE:密碼錯誤(0x31),");
								Customer_PWD = "9999999999999999"; 
								PrivateState   = "00";
								break;


							default:
								System.out.print("\nTEST CASE:Private_State="+Integer.toHexString(retry));						
								Customer_PWD = "AAAAAAAAAAAAAAAA";

								if(retry < 2) 
								{
									PrivateState = "0" + Integer.toHexString(retry);
									PrivateState.toUpperCase();
								}
								else
								{
									PrivateState   = Integer.toHexString(retry).toUpperCase();	
								}
								//System.out.println(PrivateState);
								break;
						}

						//System.out.println("PrivateState "+PrivateState.length());
						if(Customer_PWD.equals(WriteMultiBlock(Customer_PWD,18)) && PrivateState.equals(WriteMultiBlock(Integer.toHexString(retry),20)))	
						{
							System.out.print(",Customer_PWD="+Customer_PWD+",PrivateState="+PrivateState+"/RSCODE=0x"+Integer.toHexString(WriteCommand(0x33)));
						}
						else
						{
							System.out.print(",Fail when Write Customer_PWD or PrivateState into exchange area!!!");
						}
					}
					break;

				case 34:
				   System.out.println("========== 0x34 Write Tag_Type =====================");
					for(retry=1;retry<4;retry++)
					{
						switch(retry)
						{
							case 1:
								OldPWD = "9999999999999999";
								Tag_Type   = "00000001";
								break;
							case 2:
								OldPWD = "BBBBBBBBBBBBBBBB";
								Tag_Type   = "00000004";
								break;
							case 3:
								OldPWD = "BBBBBBBBBBBBBBBB";
								Tag_Type   = "00000000";
								break;

							default:
								break;
						}
						if(OldPWD.equals(WriteMultiBlock(OldPWD,18)) && Tag_Type.equals(WriteMultiBlock(Tag_Type,20)))
						{
							System.out.println("Round[34"+retry+"]/0x20/Userstar_PWD="+OldPWD+"/Tag_Type="+Tag_Type+"/RSCODE=0x"+Integer.toHexString(WriteCommand(0x34)));
						}
						else
						{
							System.out.println("Not OK");
						}
					}
					break;
				case 16:
					PWD  = "00000000";
					All = PhoneNo+RecSt+RecNo+PWD+SetupPWD;
					System.out.println("All is "+All);
					WriteMultiBlock(All,18);

					System.out.println("RSCODE for 0x16 is 0x"+Integer.toHexString(WriteCommand(0x16)));
					TriG = ReadMultiBlock(20,16);
					break;
				case 12:
					RecNo = "05";
					WriteMultiBlock(SetupPWD,18);
					WriteMultiBlock(RecNo,20);
					System.out.println("RSCODE for 0x12 is 0x"+Integer.toHexString(WriteCommand(0x12))+",PhoneNo="+ReadMultiBlock(18,16).substring(0,13));
					break;
				case 18:
					//TriG = "033B4F6FC2C065DF";//OK
					PWD  = "00000000";
					//TriG = "033B4F6FC2C065DF";
					//PhoneNo = "000935521999";
					
					All = PWD + TriG + PhoneNo + "80" + "05";
					WriteMultiBlock(All,18);
					System.out.println("RSCODE for 上鎖(0x18) is 0x"+Integer.toHexString(WriteCommand(0x18)));
					break;
				case 181:
					//TriG = "033B4F6FC2C065DF";//OK
					PWD  = "00000001";
					//TriG = "033B4F6FC2C065DF";
					//PhoneNo = "000935521999";
					
					All = PWD + TriG + PhoneNo + "80" + "05";
					WriteMultiBlock(All,18);
					System.out.println("RSCODE for 上鎖(0x18) is 0x"+Integer.toHexString(WriteCommand(0x18)));
					break;

				case 17:
					//TriG = "033B4F6FC2C065DF";//OK
					//TriG = "033B4F6FC2C065DA";
					
					All = PWD + TriG + PhoneNo + "80" + "05";
					WriteMultiBlock(All,18);
					System.out.println("RSCODE for 開鎖(0x17) is 0x"+Integer.toHexString(WriteCommand(0x17)));
					break;

				case 19:
					WriteMultiBlock("0000000000000000",18);
					WriteMultiBlock("00000005",20);
					System.out.println("RSCODE for 0x19 is 0x"+Integer.toHexString(WriteCommand(0x19)));
					break;

				case 21:
				   System.out.println("========== 0x21 Update Customer_PWD =====================");
					for(retry=1;retry<6;retry++)
					{
						switch(retry)
						{
							case 1:
								OldPWD = "9999999999999999";
								NewPWD = "0000000000000000";
								break;
							case 2:
								OldPWD = "0000000000000000";
								NewPWD = "0000000000000000";
								break;
							case 3:
								OldPWD = "0000000000000000";
								NewPWD = "AAAAAAAAAAAAAAAA";
								break;
							case 4:
								OldPWD = "AAAAAAAAAAAAAAAA";
								NewPWD = "0123456789ABCDEF";
								break;
							case 5:
								OldPWD = "0123456789ABCDEF";
								NewPWD = "AAAAAAAAAAAAAAAA";
								break;

							default:
								break;
						}
						if(OldPWD.equals(WriteMultiBlock(OldPWD,18)) && NewPWD.equals(WriteMultiBlock(NewPWD,20)))
						{
							System.out.println("Round[21"+retry+"],OldCPWD="+OldPWD+",RSCODE=0x"+Integer.toHexString(WriteCommand(0x21))+"/NewPWD="+ReadMultiBlock(18,16)+","+getTime());
							Customer_PWD = ReadMultiBlock(18,16);
						}
						else
						{
							System.out.println("Not OK");
						}
					}
					break;

				case 31:
				   System.out.println("========== 0x31 Update Userstar_PWD =====================");
					for(retry=1;retry<6;retry++)
					{
						switch(retry)
						{
							case 1:
								OldPWD = "9999999999999999";
								NewPWD = "0000000000000000";
								break;
							case 2:
								OldPWD = "0123456789ABCDEF";
								NewPWD = "0000000000000000";
								break;
							case 3:
								OldPWD = "0123456789ABCDEF";
								NewPWD = "BBBBBBBBBBBBBBBB";
								break;
							case 4:
								OldPWD = "BBBBBBBBBBBBBBBB";
								NewPWD = "0123456789ABCDEF";
								break;
							case 5:
								OldPWD = "0123456789ABCDEF";
								NewPWD = "BBBBBBBBBBBBBBBB";
								break;

							default:
								break;
						}
						if(OldPWD.equals(WriteMultiBlock(OldPWD,18)) && NewPWD.equals(WriteMultiBlock(NewPWD,20)))
						{
							System.out.println("Round[31"+retry+"],OldCPWD="+OldPWD+",RSCODE=0x"+Integer.toHexString(WriteCommand(0x31))+",NewPWD="+ReadMultiBlock(18,16)+","+getTime());
							Userstar_PWD = ReadMultiBlock(18,16);
						}
						else
						{
							System.out.println("Not OK");
						}
					}
					break;

				case 0:
					writeRs232(Inventory,Inventory[0]);
					System.out.println("UID:" + UID+"//USID:"+USID+"//KEYS:"+KEYS);

					if((UID !=null && UID.length() >0) && (KEYS !=null && KEYS.length() >0) && (USID !=null && USID.length() >0))
					{
						System.out.println("Result of upload:" + upload());
					}
					break;

				case 1:
					int rate = 0;
					System.out.println("-------------TEST Item 1:UID  Read--------------");
					for(retry=0;retry < 10;retry++)
					{
						UID = "";
						writeRs232(Inventory,Inventory[0]);
				  		//System.out.println("Write DB "+ReadUID2DB(1,"99999"));
						//System.out.println("Round["+retry+"]='"+UID+"','"+getTime()+"'/Result of Write to DB is "+ReadUID2DB(retry,UID));
						if(UID == null || UID.length() ==0) rate ++;
					}
					System.out.println("Counter of Fail times = "+ (rate ));

					break;

				case 2:
					System.out.println("-------------TEST Item 2:Block Read--------------");
					for(retry=0;retry < 28;retry++)
					{
						System.out.println("Block["+retry+"]="+ReverseData(ReadMultiBlock(retry,8)));
					}
					break;
				case 3:
				  System.out.println("-------------TEST Item 3:Block Write--------------");
				  int y;
				  for(y=0;y<10;y++)
				  {
					for(retry=0;retry < 28;retry++)
					{
						WBlock[2] = retry;
						WBlock[3] =  0x77;//retry;
						WBlock[4] =  0x77;//retry+1;
						WBlock[5] =  0x77;//retry+2;
						WBlock[6] =  0x77;//retry+3;
						System.out.print("\nRound["+y+"] Write into Block["+retry+"]="+	Integer.toHexString(WBlock[3])+
													Integer.toHexString(WBlock[4])+
													Integer.toHexString(WBlock[5])+
													Integer.toHexString(WBlock[6]));
						writeRs232(WBlock,WBlock[0]);

						System.out.print("/"+ReverseData(ReadMultiBlock(retry,8))+"/"+getTime());
					}
				  }
					break;
				case 4:
					System.out.println("-------------TEST Item 4:Block Blanking-----------");
					for(retry=0;retry < 28;retry++)
					{
						WBlock[2] = retry;
						WBlock[3] = 0x00;
						WBlock[4] = 0x00;
						WBlock[5] = 0x00;
						WBlock[6] = 0x00;
						writeRs232(WBlock,WBlock[0]);
						System.out.println("Blanking Block["+retry+"]/Read Block["+retry+"]="+ReadMultiBlock(retry,8));
					}
					break;

				case 5:
					System.out.println("-------------TEST Item 5:Block lock-----------");
					for(retry=0;retry < 28;retry++)
					{
						LBlock[2] = retry;
						LBlock[3] = 0x00;
						LBlock[4] = 0x00;
						writeRs232(LBlock,LBlock[0]);

						System.out.println("Lock Block["+retry+"]/Write Block["+retry+"]=88888888/Read Block["+retry+"]="+WriteMultiBlock("88888888",retry)+"/"+getTime());
						
					}
					break;
				case 99:
					System.exit(0);
					break;

				default:
					break;
			
			}
		}
 	
	} 


	public Verification() 
	{
		try 
		{
	    		serialPort = (SerialPort) portId.open("VerificationApp", 2000);
		} catch (PortInUseException e) {}

		try 
		{
	   		inputStream = serialPort.getInputStream();
	    		outputStream = serialPort.getOutputStream();
		} catch (IOException e) {}

		try 
		{
	    		serialPort.addEventListener(this);
		} catch (TooManyListenersException e) {}

		serialPort.notifyOnDataAvailable(true);

		try
		{
	    		serialPort.setSerialPortParams(115200, SerialPort.DATABITS_8, 
					   SerialPort.STOPBITS_1, 
					   SerialPort.PARITY_NONE);

		} catch (UnsupportedCommOperationException e) {}

		readThread = new Thread(this);
		readThread.start();
	}



	public void run() 
	{
		try
		{
	    		Thread.sleep(20000);
		} catch (InterruptedException e) {}
    	} 



	public void serialEvent(SerialPortEvent event) 
	{
		switch (event.getEventType()) 
		{
			case SerialPortEvent.BI:
			case SerialPortEvent.OE:
			case SerialPortEvent.FE:
			case SerialPortEvent.PE:
			case SerialPortEvent.CD:
			case SerialPortEvent.CTS:
			case SerialPortEvent.DSR:
			case SerialPortEvent.RI:
			case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
	    			break;
			case SerialPortEvent.DATA_AVAILABLE:
	    			try 
				{
					while (inputStream.available() > 0) 
					{
		    				numBytes = inputStream.read(readBuffer);
		  				//  System.out.println(Integer.toHexString(numBytes));
					} 
		
					int i;
					if(readBuffer[0] > 0)
					{
						//System.out.println("--------------------RFID command-----------------");

						for(i=0;i<numBytes;i++)	
						{
							data[i] = (int)(readBuffer[i] & 0xff);
							//System.out.println(Integer.toHexString(readBuffer[i]&0xff));
						}

						if(checkCRC(data) && (data[1]==0x80))
						{
							switch(data[2])
							{	
								case 0x60:
									if(data[3]==0xff)
									{
										UID = "";
									}
									else
									{
										UID = ReverseData(HextoString(data,3,8));
									}
									break;
								case 0x61:
									if(data[3]==0xff)
									{
										Block = "";
									}
									else
									{							
										Block = HextoString(data,3,4);
									}
									break;
								//case 0x62:
									//if(data[3]==0xff) Block = "";
									//break; 
								default:
									break;
								
							}

						}
					}
		
	    			} catch (IOException e) {}
	   		 	break;
		}
    	} 

	public static boolean checkCRC(int[] Data)
	{
		String CRCA = Integer.toHexString((Data[Data[0]-2] * 256 ) + Data[Data[0]-1]).toUpperCase();
		String CRCB = Integer.toHexString(crc16_R2H(Data,0,Data[0]-2)).toUpperCase();

		if(CRCA.equals(CRCB))
			return true;
		else
			return false;
	}

////////////////////////////
	//// CRC16 for Host2Reader
	static public int crc16_R2H(int[] ary, int offset, int count)
	{
		int endIndex = offset + count;
		int crc = 0xFFFF;

		for (int i = offset; i < endIndex; i++)
		{
			crc = (((int) ary[i]) << 8) ^ crc;

 			for (int j = 0; j < 8; j++) 
			{
				if ((crc & 0x8000) != 0)
					crc = (crc << 1) ^ 0x1021;
				else
					crc <<= 1;
			}
		}
		return (crc & 0xffff);
	}

	//// CRC16 for Reader2Host
	static public int crc16_H2R(int[] ary, int len)
	{
		return (crc16_R2H(ary, 0, len) ^ 0xffff);
	}
//////////////////////////////
	public static int openRs232()
	{
		   

		boolean portFound = false;

		portList = CommPortIdentifier.getPortIdentifiers();

		while (portList.hasMoreElements())
		{
			portId = (CommPortIdentifier) portList.nextElement();

			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) 
			{

				if (portId.getName().equals(defaultPort)) 
				{
		    			System.out.println("Found port " + defaultPort);

					portFound = true;

					try 
					{
						serialPort = (SerialPort) portId.open("Verification", 2000);
						//System.out.println(serialPort+"|"+portId.getName());
		    			} 
					catch (PortInUseException e) 
					{
						System.out.println("Port in use.");
						continue;	
		    			} 


		    			try 
					{
						outputStream = serialPort.getOutputStream();
					} catch (IOException e)	{}


					try 
					{
						serialPort.setSerialPortParams(115200, 
						       SerialPort.DATABITS_8, 
						       SerialPort.STOPBITS_1, 
						       SerialPort.PARITY_NONE);
		    			} catch (UnsupportedCommOperationException e) {}
	

		    			try
					{
						serialPort.notifyOnOutputEmpty(true);
					} 
					catch (Exception e) 
					{
						System.out.println("Error setting event notification");
						System.out.println(e.toString());
						return 0x03; //設定錯誤
						//System.exit(-1);
					}
					return 0x00; //開啟成功
				}

			}
		}
		return 0x02; //Port not found
   	}

	public static int closeRs232()
	{
		if(portId.getName().equals(defaultPort))
		{
			serialPort.close();
			return 0x00; //關閉成功
		}
		else
		{
			return 0x01; //關閉失敗
		}
	}

	public static void writeRs232(int[] command,int charCount)
	{
		command[charCount-2] = (crc16_H2R(command,charCount - 2) >> 8) & 0xff ;
		//System.out.println(Integer.toHexString(command[charCount-2] ));
		command[charCount-1] = crc16_H2R(command,charCount - 2) & 0xff;
		//System.out.println(Integer.toHexString(command[charCount-1] ));
		try 
		{
			int i;
			for(i=0;i < charCount;i++) outputStream.write(command[i]);

		 } catch (IOException e) {}// error !!!}
		 
		//return 0x00;
		try
		{
		      	Thread.sleep(60);  // Be sure data is xferred before closing
		} catch (Exception e) {}
	}

////////////////////////////////////////////////

}




