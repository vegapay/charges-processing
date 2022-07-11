package tech.vegapay.charges.handler;

import tech.vegapay.commons.dto.policies.AllPolicies;

public interface Program {
    AllPolicies getProgramPolicy(String programId);
}
