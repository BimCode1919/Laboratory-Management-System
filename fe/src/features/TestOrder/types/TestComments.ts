// src/features/TestOrder/types/TestComments.ts

export type TestCommentDTO = {
  id: string;
  testOrderId: string;
  testResultId: string;
  userId: string;
  content: string;
  createdAt: string;
  updatedAt?: string | null;
};

export type AdminTestCommentDTO = {
  id: string;
  testOrderId: string;
  testResultId: string;
  userId: string;
  content: string;
  createdAt: string;
  updatedAt?: string | null;
  isDeleted: boolean;
};
