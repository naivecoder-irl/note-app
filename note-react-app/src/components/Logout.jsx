import React, { useEffect } from 'react';
import Footer from './Footer';
import { useAuth } from '../providers/AuthContext';
import { useNavigate } from 'react-router-dom';

/**
 * This function clears the expired tokens and give users
 * a button to go back to home page.
 * This component would be invoked by axios response interceptor
 * and handleLogout function when RP-initiated logout request is finished.
 * @return {ReactNode} Returns a JSX element that displays a message that the user has logged out.
 */
const Logout = () => {
  const navigate = useNavigate();
  const { isLoading, isAuthenticated, handleExpiredToken } = useAuth();
  const handleClick = () => {
    navigate('/', { replace: true });
  };
  useEffect(() => {
    // clean the login status when logout page is loaded
    // handleExpiredToken();
    if (!isAuthenticated) {
      handleExpiredToken();
    }
    console.log('isAuthenticated:', isAuthenticated);
  });

  if (isLoading) {
    return (
      <div>
        <h2>Loading...</h2>
      </div>
    );
  }

  if (isAuthenticated) {
    // Redirect to the HomePage if logged in
    // window.location.href = 'http://127.0.0.1:3000/';
    window.location.href = process.env.REACT_APP_BASE_URI;
  } else {
    return (
      <div className="container">
        <div className="row mt-5">
          <div className="col-md-12">
            <h2>You have logout!</h2>
          </div>
        </div>
        <div className="row mt-3">
          <div className="col-md-12">
            <button
              type="button"
              className="btn btn-primary"
              onClick={handleClick}
            >
              Home
            </button>
          </div>
        </div>
        <Footer />
      </div>
    );
  }
};

export default Logout;
