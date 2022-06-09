<?xml version="1.0" encoding="UTF-8"?>
<saml:Assertion xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion" ID="${ID}-1"
    IssueInstant="${TIMESTAMP}" Version="2.0">
    <saml:Issuer>${ISSUER_ID}</saml:Issuer>
    <saml:Subject>
        <saml:NameID
            Format="urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified">
            ${SUBJECT}
        </saml:NameID>
        <saml:SubjectConfirmation
            Method="urn:oasis:names:tc:SAML:2.0:cm:bearer">
            <saml:SubjectConfirmationData Recipient="${RECIPIENT}"
                NotOnOrAfter="${NOT_ON_OR_AFTER}" />
        </saml:SubjectConfirmation>
    </saml:Subject>
    <saml:Conditions NotBefore="${NOT_BEFORE}"
        NotOnOrAfter="${NOT_ON_OR_AFTER}">
        <saml:AudienceRestriction>
            <saml:Audience>${RECIPIENT}</saml:Audience>
        </saml:AudienceRestriction>
    </saml:Conditions>
    <saml:AuthnStatement AuthnInstant="${TIMESTAMP}">
        <saml:AuthnContext>
            <saml:AuthnContextClassRef>
                urn:oasis:names:tc:SAML:2.0:ac:classes:unspecified
            </saml:AuthnContextClassRef>
        </saml:AuthnContext>
    </saml:AuthnStatement>
</saml:Assertion>

