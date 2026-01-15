// src/features/User/types/user.types.ts

export interface UserProfileDTO {
  id: string;
  identifyNumber: string;
  email: string;
  password?: string; 
  fullName: string;
  age: number;
  dob: string;
  phoneNumber: string;
  gender: string;
  address: string;
  status: string;
}
