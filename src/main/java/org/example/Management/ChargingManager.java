package org.example.Management;

import org.example.domain.ChargingSession;

import java.util.ArrayList;
import java.util.List;

public class ChargingManager {
    private final List<ChargingSession> sessionDatabase = new ArrayList<>();

    // CREATE
    public void addSession(ChargingSession session) {
        if (session != null) {
            sessionDatabase.add(session);
        }
    }

    // READ
    public ChargingSession getSessionById(long sessionId) {
        return sessionDatabase.stream()
                .filter(s -> s.getSessionId() == sessionId)
                .findFirst()
                .orElse(null);
    }

    public List<ChargingSession> getSessionsByClientId(int clientId) {
        return sessionDatabase.stream()
                .filter(s -> s.getClientId() == clientId)
                .toList();
    }

    public List<ChargingSession> getSessionsByChargerId(int chargerId) {
        return sessionDatabase.stream()
                .filter(s -> s.getChargerId() == chargerId)
                .toList();
    }

    public List<ChargingSession> getAllSessions() {
        return new ArrayList<>(sessionDatabase);
    }

    // UPDATE
    public boolean updateSession(long sessionId, ChargingSession updatedSession) {
        if (updatedSession == null) return false;
        for (int i = 0; i < sessionDatabase.size(); i++) {
            if (sessionDatabase.get(i).getSessionId() == sessionId) {
                sessionDatabase.set(i, updatedSession);
                return true;
            }
        }
        return false;
    }

    // DELETE
    public boolean deleteSession(long sessionId) {
        return sessionDatabase.removeIf(s -> s.getSessionId() == sessionId);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ChargingManager ===\n");
        sb.append("Total Sessions: ").append(sessionDatabase.size()).append("\n");
        sessionDatabase.forEach(session -> sb.append("- SessionId: ")
                .append(session.getSessionId())
                .append(" | ClientId: ").append(session.getClientId())
                .append(" | ChargerId: ").append(session.getChargerId())
                .append(" | Mode: ").append(session.getMode())
                .append(" | Finished: ").append(session.isFinished())
                .append("\n"));
        return sb.toString();
    }
}
