import { useState, useRef, useEffect } from 'react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';

function ChatPage() {
  const [message, setMessage] = useState('Write a short poem about coding');
  const [chatLog, setChatLog] = useState('');
  const [isStreaming, setIsStreaming] = useState(false);
  const eventSourceRef = useRef(null);
  const chatEndRef = useRef(null);

  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [chatLog]);

  const startStream = () => {
    if (!message.trim()) return;

    setChatLog('');
    setIsStreaming(true);

    if (eventSourceRef.current) {
      eventSourceRef.current.close();
    }

    const encodedMessage = encodeURIComponent(message.trim());
    const url = `http://localhost:8080/chatClient/ReactivefluxChat?message=${encodedMessage}`;

    const eventSource = new EventSource(url);
    eventSourceRef.current = eventSource;

    eventSource.onmessage = (event) => {
  
         const parsedChunk = JSON.parse(event.data);      
        // 3. Extract the text field from your Chunk object
        setChatLog((prevText) => prevText + parsedChunk.text); 
    };

    

    eventSource.onerror = () => {
      console.log('Stream terminated or completed.');
      eventSource.close();
      setIsStreaming(false);
    };
  };

  const handleSubmit = (event) => {
    event.preventDefault();
    startStream();
  };

  return (
    <main className="chat-page bg-light min-vh-100 py-4">
      <div className="container">
        <div className="row justify-content-center">
          <div className="col-12 col-lg-9">
            <div className="card shadow-sm border-0 rounded-4 overflow-hidden">
              <div className="card-header bg-white border-0 py-3 px-4">
                <p className="text-primary text-uppercase fw-bold mb-1 small">AI assistant</p>
                <h1 className="h3 mb-0 text-dark">Spring AI + React Streaming</h1>
              </div>

              <div className="card-body p-3 p-md-4">
                <div className="chat-window bg-body-tertiary border rounded-4 p-3 mb-3" role="log" aria-live="polite">
                  {chatLog ? (
                    <article className="assistant-message">
                      <ReactMarkdown remarkPlugins={[remarkGfm]}>{chatLog}</ReactMarkdown>
                    </article>
                  ) : (
                    <div className="placeholder text-muted fst-italic d-flex align-items-center justify-content-center h-100">
                      AI response will stream here...
                    </div>
                  )}
                  <div ref={chatEndRef} />
                </div>

                <form className="row g-2" onSubmit={handleSubmit}>
                  <div className="col">
                    <input
                      type="text"
                      className="form-control form-control-lg"
                      value={message}
                      onChange={(event) => setMessage(event.target.value)}
                      disabled={isStreaming}
                      placeholder="Ask your AI something..."
                      aria-label="Message to send"
                    />
                  </div>
                  <div className="col-auto">
                    <button type="submit" className="btn btn-primary btn-lg px-4" disabled={isStreaming || !message.trim()}>
                      {isStreaming ? 'Streaming...' : 'Send'}
                    </button>
                  </div>
                </form>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  );
}

export default ChatPage;
