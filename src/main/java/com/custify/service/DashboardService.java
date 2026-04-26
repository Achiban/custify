package com.custify.service;

import com.custify.repository.ClientRepository;
import com.custify.repository.InteractionRepository;
import com.custify.repository.OpportuniteRepository;
import com.custify.repository.ProspectRepository;
import lombok.Getter;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private final ClientRepository clientRepository;
    private final ProspectRepository prospectRepository;
    private final OpportuniteRepository opportuniteRepository;
    private final InteractionRepository interactionRepository;

    public DashboardService(ClientRepository clientRepository,
                           ProspectRepository prospectRepository,
                           OpportuniteRepository opportuniteRepository,
                           InteractionRepository interactionRepository) {
        this.clientRepository = clientRepository;
        this.prospectRepository = prospectRepository;
        this.opportuniteRepository = opportuniteRepository;
        this.interactionRepository = interactionRepository;
    }

    public DashboardStats getStats() {
        return new DashboardStats(
                clientRepository.count(),
                prospectRepository.count(),
                opportuniteRepository.count(),
                interactionRepository.count()
        );
    }

    @Getter
    public static class DashboardStats {
        private final long clientCount;
        private final long prospectCount;
        private final long opportuniteCount;
        private final long interactionCount;

        public DashboardStats(long clientCount, long prospectCount, long opportuniteCount, long interactionCount) {
            this.clientCount = clientCount;
            this.prospectCount = prospectCount;
            this.opportuniteCount = opportuniteCount;
            this.interactionCount = interactionCount;
        }
    }
}
