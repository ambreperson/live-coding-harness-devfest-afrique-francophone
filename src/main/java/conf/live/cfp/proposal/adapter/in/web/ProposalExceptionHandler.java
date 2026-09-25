package conf.live.cfp.proposal.adapter.in.web;

import conf.live.cfp.proposal.domain.exception.InvalidProposalException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = ProposalController.class)
public class ProposalExceptionHandler {

    @ExceptionHandler(InvalidProposalException.class)
    public ResponseEntity<String> handleInvalidProposal(InvalidProposalException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
    }
}
