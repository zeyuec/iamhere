package com.obcerver.iamhere.lib;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.Header;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.Message;

/**
 * Network Connection Library
 * @author Cary Zeyue Chen
 * @update 2014-09-17
 */
@SuppressLint("HandlerLeak")
public class CHttpConnection {
    private static final String LOG_TAG = "CHttpConnection";

    public static final int SOCKET_TIMEOUT_DEFAULT = 20000;
    public static final int TIMEOUT_LONG = 20000;
    public static final int TIMEOUT_MIDDLE = 10000;
    public static final int TIMEOUT_SHORT = 5000;

    public static final int MESSAGE_ON_ERROR     = 88880;    
    public static final int MESSAGE_ON_RESPOND   = 88881;
    public static final int MESSAGE_ON_RECEIVING = 88882;
    public static final int MESSAGE_ON_SUCCESS   = 88883;
    public static final int MESSAGE_ON_EXCEPTION = 88884;
    
    public static final String ASYNC_QUEUE_FILE_NAME = "http_connection_async_queue";

    private String                         url;
    private ArrayList<NameValuePair>       params;
    private int                            socketTimeout;
    private HttpClient                     client;
    
    private PostThread                     postThread;
    private Handler                        postHandler;

    private GetThread                      getThread;
    private Handler                        getHandler;
    
    private boolean                        isCancelled, isPaused;

    private String                         downloadedContent = "";

    public CHttpConnection(String url, ArrayList<NameValuePair> params) {
        this.url = url;
        this.params = params;
        this.isCancelled = false;
        this.isPaused = false;
        this.socketTimeout = SOCKET_TIMEOUT_DEFAULT;
    }

    public CHttpConnection(String url, ArrayList<NameValuePair> params, int socketTimeout) {
        this.url = url;
        this.params = params;
        this.isCancelled = false;
        this.isPaused = false;
        this.socketTimeout = socketTimeout;
    }

    /**
     * Get Method
     */
    public void get(Handler handler) {
        this.getHandler = handler;
        getThread = new GetThread(handler);
        getThread.start();
    }

    /**
     * Post Method
     */
    public void post(Handler handler) {
        this.postHandler = handler;
        post(handler, false);
    }

    /**
     * Post with guarantee success
     * If a connection fails, we will store it in a queue and try it intervally until it success
     */
    public void post(Handler handler, boolean guaranteeSuccess) {
        this.postHandler = handler;
    	if (guaranteeSuccess) {
    		postGuaranteeSuccess();
    	} else {
    		postThread = new PostThread(handler);
    		postThread.start();
    	}
    }

    /**
     * Resume a connection
     */
    public void resume() {
        new Thread(new Runnable() {
                @Override public void run () {
                    try {
                        while (postThread.isRunning()) {
                            Thread.sleep(500);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    
                    isPaused = false;
                    
                    postHandler.post(new Runnable() {
                            @Override public void run () {
                                postThread = new PostThread(postHandler, true, downloadedContent);
                                postThread.start();
                            }
                        });
                }
                }).start();
    }

    /**
     * Pause a connection
     */
    public void pause() {
        isPaused = true;
    }

    /**
     * Cancel a connection
     */
    public void cancel() {
        isCancelled = true;
    }
    
    /**
     * Must Success Post 
     */
    private void postGuaranteeSuccess() {
    	Handler handler = new Handler() {
    		public void handleMessage(Message message) {
    			switch (message.what) {
    			case MESSAGE_ON_SUCCESS:
    				sendQueue();
    				break;
    			case MESSAGE_ON_ERROR:
    				SharedPreferences pref = CApplication.getInstance().getSharedPreferences(ASYNC_QUEUE_FILE_NAME, 0);
    				try {
    					JSONObject jsonObject = new JSONObject();
    					jsonObject = new JSONObject();
    					jsonObject.put("_url", url);
    					for (int i=0; i<params.size(); i++) {
    						NameValuePair nvp = params.get(i);
							jsonObject.put(nvp.getName(), nvp.getValue());
    					}
	    				Editor editor = pref.edit();
	    				editor.putString(Long.toString(System.currentTimeMillis()), jsonObject.toString());
	    				editor.commit();
    				} catch (JSONException e) {
						e.printStackTrace();
						return;
					}
    				break;
    			default:
    				break;
    			}
    		}
    	};
    	postThread = new PostThread(handler);
		postThread.start();
    }
    
    /**
     * Send a connection the queue
     */
    private void sendQueue() {
    	SharedPreferences pref = CApplication.getInstance().getSharedPreferences(ASYNC_QUEUE_FILE_NAME, 0);
    	Map<String, ?> list = (Map<String, ?>) pref.getAll();
    	if (list.isEmpty()) {
    		return;
    	}
    	Set<String> c = list.keySet();
    	Iterator<String> iter = c.iterator();  
    	while (iter.hasNext()) {
    		String prefKey = iter.next();
    		String jsonString = (String) list.get(prefKey);
    		Editor editor = pref.edit();
    		editor.remove(prefKey);
    		editor.commit();
    		try {
				JSONObject jsonObject = new JSONObject(jsonString);
				Iterator<?> it = jsonObject.keys();
				ArrayList<NameValuePair> postParams = new ArrayList<NameValuePair>();
				while (it.hasNext()) {
					String key = it.next().toString();
					String value = jsonObject.getString(key);
					if (key.equals("_url")) {
						this.url = value;
					} else {
						postParams.add(new BasicNameValuePair(key, value));
					}
				}
				this.params = postParams;
				postGuaranteeSuccess();
			} catch (JSONException e) {
				e.printStackTrace();
				return;
			}
    		break;
    	}
    }

    /*
     * Get Thread (Almost the same with Post Thread, could be intergrated to one funtion)
     */
    public class GetThread extends Thread {
        
        private Handler handler;
        private boolean isResume = false, isRunning = false;
        private String content = "";
        
        public GetThread(Handler handler) {
            this.handler = handler;
            this.isResume = false;
        }

        public GetThread(Handler handler, boolean isResume, String content) {
            this.handler = handler;
            this.isResume = isResume;
            this.content = content;
        }

        private void sendMessage(int what, Object obj) {
            if (handler != null) {
                Message msg = new Message();
                msg.what = what;
                msg.obj = obj;
                handler.sendMessage(msg);
            }
        }

        public boolean isRunning() {
            return isRunning;
        }
        
        @Override public void run() {
            isRunning = true;
            client = new DefaultHttpClient();
            HttpGet getMethod = new HttpGet(url);
            // HttpPost postMethod = new HttpPost(url);
            
            try {
                
                if (isResume) {
                    params.add(new BasicNameValuePair("offset", String.valueOf(downloadedContent.length())));
                }

                // if (params != null) {
                //     getMethod.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                // }
                
                HttpResponse response = client.execute(getMethod);
                StatusLine status = response.getStatusLine();
                int statusCode = status.getStatusCode();
                
                if (statusCode != 200) {
                    CLog.v("Http Post URL: " + url);
                    CLog.v("Http Post Error Code: " + statusCode);
                    sendMessage(MESSAGE_ON_ERROR, Integer.valueOf(statusCode));
                } else {
                    // The length of already downloaded data
                    int curCount = content.length();

                    // check if need to send header to resume
                    Header[] headers = response.getAllHeaders();
                    if (!isResume) {
                        sendMessage(MESSAGE_ON_RESPOND, headers);
                    }

                    // get entity
                    HttpEntity entity = response.getEntity();
                    
                    if (entity == null) {
                        sendMessage(MESSAGE_ON_ERROR, null);
                    } else {
                        // read from a stream
                        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int len = -1;
                        InputStream is = entity.getContent();
                        while ((len = is.read(buffer)) != -1 && !isCancelled && !isPaused) {
                            outStream.write(buffer, 0, len);
                            curCount += len;
                            if (!isCancelled && !isPaused) {
                                sendMessage(MESSAGE_ON_RECEIVING, Integer.valueOf(curCount));
                            }
                        }

                        // stop the scream
                        byte[] data = outStream.toByteArray();
                        outStream.close();
                        // is.close(); // the close fcuntion of input stream is empty, so we could skip this function
                        if (isCancelled) {
                            // cancel
                            downloadedContent = "";
                            client.getConnectionManager().shutdown();
                        } else if (isPaused) {
                            // pause
                            downloadedContent += new String(data, "UTF-8");
                        } else {
                            // normally stop
                            downloadedContent = "";
                            String retStr = new String(data, "UTF-8");
                            sendMessage(MESSAGE_ON_SUCCESS, content+retStr);
                        }
                    }
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                sendMessage(MESSAGE_ON_EXCEPTION, e);
            }

            isRunning = false;
        }
    }

    
    /**
     * Post Thread
     */
    public class PostThread extends Thread {
        
        private Handler handler;
        private boolean isResume = false, isRunning = false;
        private String content = "";
        
        public PostThread(Handler handler) {
            this.handler = handler;
            this.isResume = false;
        }

        public PostThread(Handler handler, boolean isResume, String content) {
            this.handler = handler;
            this.isResume = isResume;
            this.content = content;
        }

        private void sendMessage(int what, Object obj) {
            if (handler != null) {
                Message msg = new Message();
                msg.what = what;
                msg.obj = obj;
                handler.sendMessage(msg);
            }
        }

        public boolean isRunning() {
            return isRunning;
        }
        
        @Override public void run() {
            isRunning = true;
            client = new DefaultHttpClient();
            HttpPost postMethod = new HttpPost(url);
            
            try {
                
                if (isResume) {
                    params.add(new BasicNameValuePair("offset", String.valueOf(downloadedContent.length())));
                }

                if (params != null) {
                    postMethod.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                }
                HttpResponse response = client.execute(postMethod);
                StatusLine status = response.getStatusLine();
                int statusCode = status.getStatusCode();
                
                if (statusCode != 200) {
                    CLog.v("Http Post URL: " + url);
                    CLog.v("Http Post Error Code: " + statusCode);
                    sendMessage(MESSAGE_ON_ERROR, Integer.valueOf(statusCode));
                } else {
                    int curCount = content.length();

                    Header[] headers = response.getAllHeaders();
                    if (!isResume) {
                        sendMessage(MESSAGE_ON_RESPOND, headers);
                    }

                    HttpEntity entity = response.getEntity();
                    
                    if (entity == null) {
                        sendMessage(MESSAGE_ON_ERROR, null);
                    } else {
                        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int len = -1;
                        InputStream is = entity.getContent();
                        while ((len = is.read(buffer)) != -1 && !isCancelled && !isPaused) {
                            outStream.write(buffer, 0, len);
                            curCount += len;
                            if (!isCancelled && !isPaused) {
                                sendMessage(MESSAGE_ON_RECEIVING, Integer.valueOf(curCount));
                            }
                        }

                        byte[] data = outStream.toByteArray();
                        outStream.close();
                        // is.close(); 

                        if (isCancelled) {
                            downloadedContent = "";
                            client.getConnectionManager().shutdown();
                            
                        } else if (isPaused) {
                            downloadedContent += new String(data, "UTF-8");
                        } else {
                            downloadedContent = "";
                            String retStr = new String(data, "UTF-8");
                            sendMessage(MESSAGE_ON_SUCCESS, content+retStr);
                        }
                    }
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                sendMessage(MESSAGE_ON_EXCEPTION, e);
            }

            isRunning = false;
        }
    }
}
