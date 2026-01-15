package org.overcode250204.iamservice.grpc.imps;

import net.devh.boot.grpc.server.service.GrpcService;
import org.overcode250204.common.grpc.CognitoSub;
import org.overcode250204.common.grpc.IamServiceGrpc;
import org.overcode250204.common.grpc.InternalUserProfile;
import org.overcode250204.iamservice.entities.UserProfile;
import org.overcode250204.iamservice.exceptions.ErrorCode;
import org.overcode250204.iamservice.exceptions.IamServiceException;
import org.overcode250204.iamservice.repositories.UserRepository;



@GrpcService
public class IamGrpcServiceImp extends IamServiceGrpc.IamServiceImplBase {

    private final UserRepository userRepository;

    public IamGrpcServiceImp(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void getUserByCognitoSub(CognitoSub request, io.grpc.stub.StreamObserver<InternalUserProfile> responseObserver) {
        String cognitoSub = request.getCognitoSub();
        UserProfile user = userRepository.findByCognitoSub(cognitoSub).orElseThrow(() -> new IamServiceException(ErrorCode.COGNITO_SUB_DOES_NOT_EXIST));
        String userId = user.getId().toString();
        InternalUserProfile response = InternalUserProfile.newBuilder().setUserId(userId).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
