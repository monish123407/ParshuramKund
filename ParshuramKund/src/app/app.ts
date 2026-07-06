import { Component, signal, ChangeDetectorRef, inject } from '@angular/core';
import { Router, RouterOutlet, Event, NavigationStart, NavigationEnd, NavigationCancel, NavigationError } from '@angular/router';
import { Navigation } from './components/navigation/navigation';
import { CommonModule } from '@angular/common';
import { MaterialModule } from './material.module';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    MaterialModule,
    RouterOutlet,
    Navigation
  ],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  title = signal('ParshuramKund');
  isLoading = false;

  // Chatbot states
  isChatOpen = false;
  isTyping = false;
  userMessage = '';
  chatMessages: Array<{ sender: 'user' | 'bot', text: string }> = [
    {
      sender: 'bot',
      text: 'Namaste! 🙏 I am your **Parshuram Kund Mela AI Assistant**.\n\nHow can I help you today? You can ask me about registration, holy dip dates, accommodation, travel guidelines, or helpline numbers!'
    }
  ];

  private http = inject(HttpClient);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);

  constructor() {
    this.router.events.subscribe((event: Event) => {
      if (event instanceof NavigationStart) {
        this.isLoading = true;
        this.cdr.detectChanges();
      } else if (
        event instanceof NavigationEnd ||
        event instanceof NavigationCancel ||
        event instanceof NavigationError
      ) {
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  toggleChat() {
    this.isChatOpen = !this.isChatOpen;
    if (this.isChatOpen) {
      setTimeout(() => this.scrollToBottom(), 50);
    }
  }

  sendMessage(text?: string) {
    const msg = text ? text.trim() : this.userMessage.trim();
    if (!msg) return;

    if (!text) {
      this.userMessage = '';
    }

    // Add user message
    this.chatMessages.push({ sender: 'user', text: msg });
    this.isTyping = true;
    this.cdr.detectChanges();
    this.scrollToBottom();

    // Call backend
    this.http.post<any>('/api/ai/chat', { message: msg })
      .subscribe({
        next: (res) => {
          this.isTyping = false;
          this.chatMessages.push({ sender: 'bot', text: res.response });
          this.cdr.detectChanges();
          this.scrollToBottom();
        },
        error: (err) => {
          console.error(err);
          this.isTyping = false;
          this.chatMessages.push({
            sender: 'bot',
            text: 'I apologize, I am experiencing temporary difficulties. Please try again shortly.'
          });
          this.cdr.detectChanges();
          this.scrollToBottom();
        }
      });
  }

  selectSuggestedQuestion(question: string) {
    this.sendMessage(question);
  }

  scrollToBottom() {
    if (typeof document !== 'undefined') {
      const chatBody = document.getElementById('chatBody');
      if (chatBody) {
        chatBody.scrollTop = chatBody.scrollHeight;
      }
    }
  }

  formatMessage(text: string): string {
    if (!text) return '';
    
    // Escape standard HTML characters to prevent XSS
    let escaped = text
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;');
    
    // Convert headers: ### header text -> h5
    escaped = escaped.replace(/^### (.*$)/gim, '<h5 class="chat-bubble-header font-serif">$1</h5>');
    
    // Convert bold text: **bold text** -> strong
    escaped = escaped.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
    
    // Convert bullet points: - item or * item -> list div
    escaped = escaped.replace(/^\s*[-*]\s+(.*$)/gim, '<div class="chat-bullet-row"><span class="chat-bullet">•</span><span class="chat-bullet-text">$1</span></div>');
    
    // Convert double newlines to paragraph break, single newlines to line break
    escaped = escaped.replace(/\n\n/g, '<div style="margin-bottom: 8px;"></div>');
    escaped = escaped.replace(/\n/g, '<br>');
    
    return escaped;
  }
}
