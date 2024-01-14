package com.evalvis.blog.user;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

public class FakeClientRegistrationRepository implements ClientRegistrationRepository {
    @Override
    public ClientRegistration findByRegistrationId(String registrationId) {
        return ClientRegistration.withRegistrationId(registrationId)
                .clientId("fakeClientId")
                .clientSecret("fakeClientSecret")
                .build();
    }
}
