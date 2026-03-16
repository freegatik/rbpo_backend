package ru.rbpo.backend.dto;

/**
 * Ответ с тикетом и ЭЦП на его основе.
 */
public class TicketResponse {

    private Ticket ticket;
    /** Подпись (ЭЦП) тикета — HMAC-SHA256 в Base64 */
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
