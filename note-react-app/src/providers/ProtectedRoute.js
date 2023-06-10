/* eslint-disable react/prop-types */
import { Navigate } from 'react-router-dom';
import { useAuth } from './AuthContext';

export const ProtectedRoute = ({ children }) => {
  const { isLoading, isAuthenticated } = useAuth();
  // console.log('isAuthenticated:', isAuthenticated);
  if (isLoading) {
    return (
      <div>
        <h1>Loading...</h1>
      </div>
    );
  }

  if (!isAuthenticated) {
    // Redirect to the HomePage
    return <Navigate to="/" replace />;
  }
  return children;
};
