package org.project.domain.communication.value_objects;

import org.project.domain.shared.value_objects.OwnerID;
import org.project.domain.shared.value_objects.UserID;

import static org.project.domain.shared.util.Utils.required;

public sealed interface Participant {

    UserID userID();

    ParticipantType type();

    enum ParticipantType {
        OWNER, USER
    }

    record OwnerParticipant(OwnerID ownerID) implements Participant {
        public OwnerParticipant {
            required("ownerID", ownerID);
        }

        public ParticipantType type() {
            return ParticipantType.OWNER;
        }

        @Override
        public UserID userID() {
            return new UserID(ownerID.value());
        }
    }

    record UserParticipant(UserID userID) implements Participant {
        public UserParticipant {
            required("userID", userID);
        }

        public ParticipantType type() {
            return ParticipantType.USER;
        }
    }
}
