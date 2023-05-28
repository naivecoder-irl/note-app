// import PropTypes from 'prop-types';
import React from 'react';
import Footer from './Footer';
import { useAuth } from '../providers/AuthContext';
import { useNavigate } from 'react-router-dom';
// import * as Constants from '../constants/config';

const Logout = () => {
  const navigate = useNavigate();
  const handleClick = () => {
    //window.location.href = 'http://127.0.0.1:3000';
    navigate('/', { replace: true });
  };

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
};

export default Logout;
