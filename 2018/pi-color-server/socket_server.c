/* 
 * See README.md for more.
 */ 

#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <errno.h>
#include <string.h>
#include <unistd.h>
#include <time.h>

#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>

#include <wiringPi.h>
#include <wiringPiSPI.h>

#include "p9813.h" // TCL Lighting library

int mode = 0;

uint8_t makeFlag(uint8_t red, uint8_t green, uint8_t blue) {
  uint8_t flag = 0;

  flag = (red&0xC0)>>6;  // (0xff & 0xC0) >> 6 = 0b1111 1111 & 0b1100 0000 >> 6 = 0b1100 0000 >>6 = 0b0000 0011
	// flag = 0b0000 0011  at this point if red is 0xff
  flag = flag | ((green&0xC0)>>4); // 0b0000 0011 | ((0b0000 0000 & 0b1100 0000) >> 4) = 0b0000 0011 | ((0b0000 0000) >> 4) = 0b0000 0011
	// flag = 0b0000 0011 at this point if green is 0x00 and red is 0xff
  flag = flag | ((blue&0xC0)>>2); // same as last time... since blue is 0x00 also
	// flag = 0b0000 0011 at this point if green is 0x00 and red is 0xff
  return ~flag; // inversion! so 0b0000 0011 goes to 0b1111 1100  =  0xfc
}

int sendFrame(unsigned char *data, int length) {
	return wiringPiSPIDataRW(0, data, length);
}

int sendEmptyFrame() {
	uint8_t empty_data [4];
	if (mode == 0) {
		empty_data[0] = 0;
		empty_data[1] = 0;
		empty_data[2] = 0;
		empty_data[3] = 0;
		return sendFrame(empty_data, 4);
	} else {
		empty_data[0] = 0;
		empty_data[1] = 0;
		empty_data[2] = 0;
		return sendFrame(empty_data, 3);
	}
}

int sendColor(uint8_t red, uint8_t green, uint8_t blue) {
	uint8_t color_data [4];
	if (mode == 0) { // total control lighting is strange
		color_data[0] = makeFlag(red, green, blue);
		color_data[1] = blue;
		color_data[2] = green;
		color_data[3] = red; // red last!? Yeah, crazy, I know. deal with it.
		return sendFrame(color_data, 4);
	} else { // sparkfun strip is sane
		color_data[0] = red;
		color_data[1] = green;
		color_data[2] = blue; 
		return sendFrame(color_data, 3);
	}
}

uint8_t parseRed(char* color) {
	char red_color[3];
	unsigned int to_ret;
	red_color[0] = color[0];
	red_color[1] = color[1];
	red_color[2] = 0;
	sscanf(red_color, "%x", & to_ret);
	return to_ret;
}
uint8_t parseGreen(char* color) {
	char green_color[3];
	unsigned int to_ret;
	green_color[0] = color[2];
	green_color[1] = color[3];
	green_color[2] = 0;
	sscanf(green_color, "%x", & to_ret);
	return to_ret;
}
uint8_t parseBlue(char* color) {
	char blue_color[3];
	unsigned int to_ret;
	blue_color[0] = color[4];
	blue_color[1] = color[5];
	blue_color[2] = 0;
	sscanf(blue_color, "%x", & to_ret);
	return to_ret;
}

void str_echo(int sockfd) {
	char buff[4900];
	ssize_t n = 0;
	
	char * pch;
	
	uint8_t red, green, blue;
	
	while(1) {
		bzero(&buff, sizeof(buff));
		n = read(sockfd,buff,4900);
		
		if(n>0) {
			//write(sockfd,buff,20);
			//printf("Client sent %i characters... writing any colors we find.\n", n);
			
			// parse the colors into an array for the leds (maximum of 1000 leds)
			
			//printf("Writing colors to SPI channel 0\n");
			if (mode == 0) {
				sendEmptyFrame();
			}
			
			pch = strtok(buff," \n");
			while (pch != NULL){
				//printf("color: %s\n", pch);
				red = parseRed(pch);
				green = parseGreen(pch);
				blue = parseBlue(pch);
				sendColor(red, green, blue);
				pch = strtok(NULL, " \n");
			}
			
			if (mode == 0) {
				sendEmptyFrame();
			}
			//printf("Colors written\n");
			
		} else if (n == 0) {
			//write(sockfd,"blank",20);
			printf("Client disconnected. Killing this child...\n");
			exit(0);
		}
		//else
		  //printf("%s\n",buff);        // if i replace it with printf("%s",buff) then it wont work
		  // write(sockfd,buff,20);
		   //return;
		//else
		  // writen(sockfd,buff,n);
	}
}

int testTCL(int argc, char *argv[]) {
	int     i,totalPixels,
	  nStrands        = 1,
	  pixelsPerStrand = 25;
	TCstats stats;
	TCpixel *pixelBuf;

	while((i = getopt(argc,argv,"s:p:")) != -1)
	{
		switch(i)
		{
		   case 's':
			nStrands        = strtol(optarg,NULL,0);
			break;
		   case 'p':
			pixelsPerStrand = strtol(optarg,NULL,0);
			break;
		   case '?':
		   default:
			(void)printf(
			  "usage: %s [-s strands] [-p pixels]\n",argv[0]);
			return 1;
		}
	}

	/* Allocate pixel array (one TCpixel per pixel per strand). */
	totalPixels = nStrands * pixelsPerStrand;
	i           = totalPixels * sizeof(TCpixel);
	if(NULL == (pixelBuf = (TCpixel *)malloc(i)))
	{
		printf("Could not allocate space for %d pixels (%d bytes).\n",
		  totalPixels,i);
		return 1;
	}

	/* Initialize library, open FTDI device.  Baud rate errors
	   are non-fatal; program displays a warning but continues. */
	if((i = TCopen(nStrands,pixelsPerStrand)) != TC_OK)
	{
		TCprintError(i);
		if(i < TC_ERR_DIVISOR) return 1;
	}

	/* Initialize statistics structure before use. */
	TCinitStats(&stats);

	/* Seed the random number generator with the current system time,
	   then set all pixels on all strands to random RGB values. */
	srand(time(NULL));
	for(i=0;i<totalPixels;i++)
		pixelBuf[i] = TCrgb(rand()&255,rand()&255,rand()&255);

	if((i = TCrefresh(pixelBuf,NULL,&stats)) == TC_OK)
		TCprintStats(&stats);
	else
		TCprintError(i);

	TCclose();
	free(pixelBuf);
	return 0;
}

int main (int argc, char* argv[]) {
	int channel = 0;
	
	testTCL(argc, argv);

	if (argc != 1) {
		printf("More then one argument given! Switching to sparkfun strip mode... \n");
		mode = 1;
	} else {
		printf("No arguments given. Switching to Total Control Lighting strand mode... \n");
	}
	
	if (wiringPiSetup () == -1) {
		fprintf (stdout, "oops: %s\n", strerror (errno)) ;
		return 1 ;
	}

	if (wiringPiSPISetup (channel, 1000000) < 0) {
		fprintf (stderr, "SPI Setup failed: %s\n", strerror (errno));
	}
	
	// Start of socket code:
	int sockfd, connfd, childpid;
	unsigned int clilen;
	struct sockaddr_in cliaddr, servaddr;

	sockfd = socket(AF_INET, SOCK_STREAM, 0); // setup the socket descriptor (0 is probably wrong... 6 = tcp?)

	// Build the server address to listen on...
	bzero(&servaddr, sizeof(servaddr)); // zero out the server address
	servaddr.sin_family = AF_INET; // set to ipv4 internet protocol
	servaddr.sin_addr.s_addr = htonl(INADDR_ANY); // listen on any local address
	servaddr.sin_port = htons(3333); // set the port
	
	// bind to the address and port we just set up and set the socket file descriptor to sockfd
	bind(sockfd, (struct sockaddr*) &servaddr, sizeof(servaddr)); 
	printf("Bound to socket...\n");
	
	listen(sockfd,5); // listen on the socket descriptor we just bound to.
	printf("Listening on port 3333...\n");
	
	while(1) {
		clilen = sizeof(cliaddr); // set length of client address
		
		// accept a client connection on socket descriptor and setup a file desciptor for that connection
		connfd = accept(sockfd, (struct sockaddr*) &cliaddr, &clilen); 
		
		printf("Connection accepted\n");
		if((childpid=fork())==0) { // Fork! Gah! And return the childs process id
			close(sockfd); // close the socket descriptor 
			printf("Child process started for SPI colors...\n");
			str_echo(connfd);
			// str_echo(connfd);
			exit(0);
		}
		printf("Connection established\n");
		close(connfd);
	}
	
	return 0;
}
