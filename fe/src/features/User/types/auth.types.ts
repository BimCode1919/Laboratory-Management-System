export interface LoginDTO {
  identifyNumber: string;
  password: string;
}

export interface JwtTokenDTO {
  accessToken: string;
  idToken: string;
  refreshToken: string;
  expiresIn: number;
  firstLogin: boolean;
  session: string;
  deviceId: string;
}