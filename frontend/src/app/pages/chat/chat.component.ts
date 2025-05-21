import { Component, OnInit } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { CommonModule, NgClass } from "@angular/common";
import { FormsModule } from "@angular/forms";

@Component({
  selector: "app-chat",
  standalone: true,
  imports: [NgClass, FormsModule, CommonModule],
  templateUrl: "./chat.component.html",
  styleUrls: ["./chat.component.css"],
})
export class ChatComponent implements OnInit {
  chatMessages: { sender: string; content: string; isSystem?: boolean }[] = [];
  socket?: WebSocket;
  connected = false;
  chatMsg = { content: "" };


  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.connect();
  }

  connect(): void {
    this.socket = new WebSocket("ws://localhost:8082/chat");

    this.socket.onopen = () => {
      this.connected = true;
    };

    this.socket.onmessage = (event) => {
      const msg = JSON.parse(event.data);
      this.appendMessage(msg.sender, msg.content, false);
    };

    this.socket.onclose = () => {
      this.connected = false;
    };
  }

  sendMessage(): void {
    if (!this.chatMsg.content.trim()) return;

    const headers = {
      Authorization: "Bearer " + localStorage.getItem("Token"),
    };

    this.http
      .post("http://localhost:8081/api/user/chat", this.chatMsg, {
        headers,
        responseType: "text",
      })
      .subscribe({
        next: () => {
          this.chatMsg.content = "";
        },
        error: (err) => {
          console.error("Eroare la trimiterea mesajului:", err);
        },
      });
  }

  appendMessage(sender: string, content: string, isSystem = false): void {
    if (content.trim().toLowerCase() === "hit" ||content.trim().toLowerCase() === "stand") {
      return;
    }
    const formattedSender =
      sender.charAt(0).toUpperCase() + sender.slice(1).toLowerCase();

    this.chatMessages.push({
      sender: formattedSender,
      content,
      isSystem,
    });

    setTimeout(() => {
      const mesQueue = document.getElementById("chatMessages");
      if (mesQueue) mesQueue.scrollTop = mesQueue.scrollHeight;
    }, 100);
  }
}
