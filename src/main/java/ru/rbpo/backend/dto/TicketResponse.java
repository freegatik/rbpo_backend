package ru.rbpo.backend.dto;

/** Ответ activate/check/renew: тикет + подпись ЭЦП (Base64). */
public class TicketResponse {

    private Ticket ticket;
    private String signature;

    public TicketResponse() {
    }

    public TicketResponse(Ticket ticket, String signature) {
        this.ticket = ticket;
        this.signature = signature;
    }

    public Ticket getTicket() { return ticket; }
    public void setTicket(Ticket ticket) { this.ticket = ticket; }
    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }
}
