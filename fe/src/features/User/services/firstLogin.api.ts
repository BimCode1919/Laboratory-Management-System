import axios from "../../../api/AxiosInstance";

export interface FirstLoginDTO {
  identifyNumber: string;
  newPassword: string;
  session: string;
}

export const confirmFirstLoginApi = async (payload: FirstLoginDTO) => {
  const response = await axios.post(
    "/iam-service/first-login/change-password",
    payload,
    {
      headers: { "Content-Type": "application/json" },
    }
  );
  return response.data;
};
