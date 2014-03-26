package com.zinio.test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ProxiedZombiePortSimulator {
	private class KeyboardReader extends Thread {
		private Thread inboundMessages;
		private Thread outboundMessages;

		public KeyboardReader(Thread inboundMessages, Thread outboundMessages) {
			super();
			this.inboundMessages = inboundMessages;
			this.outboundMessages = outboundMessages;
		}

		@Override
		public void run() {
			try {
				System.in.read();
				simulateHungPort = true;
				System.out.println("**** Simulating Hung Port ****");
				System.in.read();
				System.out.println("**** Exiting ****");
				exitThreads = true;
				inboundMessages.interrupt();
				outboundMessages.interrupt();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private class InboundMessages extends Thread {
        private Socket listenerSocket = null;
        private Socket destinationSocket = null;

        public InboundMessages(Socket listenerSocket, Socket destinationSocket) {
			super();
			this.listenerSocket = listenerSocket;
			this.destinationSocket = destinationSocket;
		}

		@Override
		public void run() {
        	DataInputStream listenerDataInputStream = null;
        	DataOutputStream destinationDataOutputStream = null;
            try {
                listenerDataInputStream = new DataInputStream(listenerSocket.getInputStream());
                destinationDataOutputStream = new DataOutputStream(destinationSocket.getOutputStream());
	            int counter1 = 0;
	            int counter2 = 0;
	            int counter3 = 0;
	            while (true) {
	                if (simulateHungPort && noRead) {
	                    continue;
	                }
	                byte dataByte = listenerDataInputStream.readByte();
	                if (++counter1 % 32 == 0) {
	                    System.out.print((++counter2 % 32 != 0) ? "." : ++counter3 + "k");
	                }
	                if (!simulateHungPort) {
	                    destinationDataOutputStream.write(dataByte);
	                }
	            }
			} catch (IOException ioe) {
				ioe.printStackTrace();
			} finally {
	            if (null != listenerDataInputStream) {
	                try {
	                	listenerDataInputStream.close();
	                } catch (IOException ioe) {
	                    ioe.printStackTrace();
	                }
	            }
	            if (null != destinationDataOutputStream) {
	                try {
	                	destinationDataOutputStream.close();
	                } catch (IOException ioe) {
	                    ioe.printStackTrace();
	                }
	            }
				inboundMessagesCompleted = true;
			}
		}
	}

	private class OutboundMessages extends Thread {
        private Socket listenerSocket = null;
        private Socket destinationSocket = null;

		public OutboundMessages(Socket listenerSocket, Socket destinationSocket) {
			super();
			this.listenerSocket = listenerSocket;
			this.destinationSocket = destinationSocket;
		}

		@Override
		public void run() {
        	DataInputStream destinationDataInputStream = null;
        	DataOutputStream listenerDataOutputStream = null;
            try {
                listenerDataOutputStream = new DataOutputStream(listenerSocket.getOutputStream());
            	destinationDataInputStream = new DataInputStream(destinationSocket.getInputStream());
	            while (!exitThreads) {
	                if (simulateHungPort) {
	                    break;
	                }
	                byte dataByte = destinationDataInputStream.readByte();
	                listenerDataOutputStream.write(dataByte);
	            }
			} catch (IOException ioe) {
				ioe.printStackTrace();
			} finally {
	            if (null != destinationDataInputStream) {
	                try {
	                	destinationDataInputStream.close();
	                } catch (IOException ioe) {
	                    ioe.printStackTrace();
	                }
	            }
	            if (null != listenerDataOutputStream) {
	                try {
	                	listenerDataOutputStream.close();
	                } catch (IOException ioe) {
	                    ioe.printStackTrace();
	                }
	            }
				outboundMessagesCompleted = true;
			}
		}
	}

	private boolean noRead;
	private boolean simulateHungPort = false;
    private int listenerPort;
    private int destinationPort;
    private boolean inboundMessagesCompleted = false;
    private boolean outboundMessagesCompleted = false;
    private boolean exitThreads = false;

    public ProxiedZombiePortSimulator(boolean noRead, int listenerPort, int destinationPort) {
        this.noRead = noRead;
        this.listenerPort = listenerPort;
        this.destinationPort = destinationPort;
    }

    public void run() {
        ServerSocket serverSocket = null;
        Socket listenerSocket = null;
        Socket destinationSocket = null;
        KeyboardReader keyboardReader = null;
        try {
            serverSocket = new ServerSocket(listenerPort);
            listenerSocket = serverSocket.accept();
            destinationSocket = new Socket(InetAddress.getLocalHost(), destinationPort);
            InboundMessages inboundMessages = new InboundMessages(listenerSocket, destinationSocket);
            inboundMessages.start();
            OutboundMessages outboundMessages = new OutboundMessages(listenerSocket, destinationSocket);
            outboundMessages.start();
            keyboardReader = new KeyboardReader(inboundMessages, outboundMessages);
            keyboardReader.start();
            while (!(inboundMessagesCompleted && outboundMessagesCompleted))
            	;
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (IllegalThreadStateException itse) {
        	itse.printStackTrace();
        } finally {
            if (null != listenerSocket) {
                try {
                    listenerSocket.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
            if (null != serverSocket) {
                try {
                    serverSocket.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
            if (null != destinationSocket) {
                try {
                	destinationSocket.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
            if (null != keyboardReader) {
            	keyboardReader.interrupt();
            }
        }
    }

    public static void main(String[] args) {
    	if (null == args || 0 == args.length) {
    		System.err.println("Usage: java -jar ProxiedZombiePortSimulator.jar [read|noread] [listener port] [destination port]");
    		System.exit(-1);
    	}
        boolean noRead = !args[0].equalsIgnoreCase("read");
        int listenerPort = Integer.parseInt(args[1]);
        int destinationPort = Integer.parseInt(args[2]);
        ProxiedZombiePortSimulator simulator = new ProxiedZombiePortSimulator(noRead, listenerPort, destinationPort);
        simulator.run();
    }
}
