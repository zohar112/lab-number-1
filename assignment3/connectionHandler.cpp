#include "../include/connectionHandler.h"
#include <boost/asio/ip/tcp.hpp>
using boost::asio::ip::tcp;

using std::cin;
using std::cout;
using std::cerr;
using std::endl;
using std::string;
 
ConnectionHandler::ConnectionHandler(string host, short port): host_(host), port_(port), io_service_(), socket_(io_service_){}
    
ConnectionHandler::~ConnectionHandler() {
    close();
}
 
bool ConnectionHandler::connect() {
    std::cout << "Starting connect to " 
        << host_ << ":" << port_ << std::endl;
    try {
		tcp::endpoint endpoint(boost::asio::ip::address::from_string(host_), port_); // the server endpoint
		boost::system::error_code error;
		socket_.connect(endpoint, error);
		if (error)
			throw boost::system::system_error(error);
    }
    catch (std::exception& e) {
        std::cerr << "Connection failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}
 
bool ConnectionHandler::getBytes(char bytes[], unsigned int bytesToRead) {
    size_t tmp = 0;
	boost::system::error_code error;
    try {
        while (!error && bytesToRead > tmp ) {
			tmp += socket_.read_some(boost::asio::buffer(bytes+tmp, bytesToRead-tmp), error);			
        }
		if(error)
			throw boost::system::system_error(error);
    } catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

bool ConnectionHandler::sendBytes(const char bytes[], int bytesToWrite) {
    int tmp = 0;
	boost::system::error_code error;
    try {
        while (!error && bytesToWrite > tmp ) {
			tmp += socket_.write_some(boost::asio::buffer(bytes + tmp, bytesToWrite - tmp), error);
        }
		if(error)
			throw boost::system::system_error(error);
    } catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}
 
bool ConnectionHandler::getLine(std::string& line) {
    return getFrameAscii(line, ';');
}

bool ConnectionHandler::sendLine(std::string& line) {
    return sendFrameAscii(line, ';');
}
 
bool ConnectionHandler::getFrameAscii(std::string& frame, char delimiter) {  //byte to short--> read ack/not/error
    char ch;
    // Stop when we encounter the null character.
    // Notice that the null character is not appended to the frame string.
    try {
		do{
            getBytes(&ch, 1);
            frame.append(1, ch);
        }
        while (delimiter != ch);
    }
    catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    char shortByte[2]= {frame[0], frame[1]};
    short sh = bytesToShort(shortByte);
    std::string result="";
    if(sh==9)
        result+= "NOTIFICATION";
    else if(sh==10)
        result+= "ACK";
    else if(sh==11)
        result+= "ERROR";

    for(unsigned int i=2; i<frame.length(); i++) {
         if(frame[i]==0)
            result = result+ " ";
         else result = result + frame[i];
    }
    frame=result;
    return true;
}
 
bool ConnectionHandler::sendFrameAscii(const std::string& frame, char delimiter) { //short to byte
    short sh;
    bool result =true;
    int tmp= frame.find(" ");
    if(tmp !=-1) {
        std::string command = frame.substr(0, tmp);
        if (command == "REGISTER")
            sh = 01;
        else if (command == "LOGIN")
            sh = 02;
        else if (command == "LOGOUT")
            sh = 03;
        else if (command == "FOLLOW")
            sh = 04;
        else if (command == "POST")
            sh = 05;
        else if (command == "PM")
            sh = 06;
        else if (command == "LOGSTAT")
            sh = 07;
        else if (command == "STAT")
            sh = 8;
        else if (command == "BLOCK")
            sh = 12;

        char shortByte[2];
        shorToByte(sh, shortByte);
        std::string content = frame.substr(tmp + 1);
        result = sendBytes(shortByte, 2);
        result = sendBytes(content.c_str(), content.length());
    }
    else if (frame =="LOGSTAT") {
        sh = 07;
        char shortByte[2];
        shorToByte(sh, shortByte);
        sendBytes(shortByte, 2);
    }
    else if (frame =="LOGOUT") {
        sh = 03;
        char shortByte[2];
        shorToByte(sh, shortByte);
        sendBytes(shortByte, 2);
    }
    if(!result) return false;
    return sendBytes(&delimiter,1);
}

// Close down the connection properly.
void ConnectionHandler::close() {
    try{
        socket_.close();
    } catch (...) {
        std::cout << "closing failed: connection already closed" << std::endl;
    }
}

short ConnectionHandler::bytesToShort(char *bytesArr) {
    short result = (short)((bytesArr[0] & 0xff) << 8);
    result += (short)(bytesArr[1] & 0xff);
    return result;
}
void ConnectionHandler::shorToByte(short num, char *bytesArr) {
    bytesArr[0] = ((num >> 8) & 0xFF);
    bytesArr[1] = (num & 0xFF);
}

