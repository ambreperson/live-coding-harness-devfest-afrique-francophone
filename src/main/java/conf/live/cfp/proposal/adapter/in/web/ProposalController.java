package conf.live.cfp.proposal.adapter.in.web;

import conf.live.cfp.proposal.application.port.in.CreateProposalCommand;
import conf.live.cfp.proposal.application.port.in.CreateProposalUseCase;
import conf.live.cfp.proposal.domain.model.Proposal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * Primary adapter exposing the proposal creation use case over HTTP.
 */
@RestController
@RequestMapping("/api/proposals")
public class ProposalController {

    private final CreateProposalUseCase createProposalUseCase;

    public ProposalController(CreateProposalUseCase createProposalUseCase) {
        this.createProposalUseCase = createProposalUseCase;
    }

    @PostMapping
    public ResponseEntity<ProposalResponse> createProposal(@Valid @RequestBody CreateProposalRequest request) {
        Proposal created = createProposalUseCase.createProposal(new CreateProposalCommand(
                request.title(), request.description(), request.speakerName(), request.speakerEmail(),
                request.eventId()));
        ProposalResponse response = ProposalResponse.from(created);
        return ResponseEntity.created(URI.create("/api/proposals/" + response.id())).body(response);
    }
}
