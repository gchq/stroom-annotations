import React from 'react'
import PropTypes from 'prop-types'

import CircularProgress from 'material-ui/CircularProgress';

const ApiCallSpinner = ({isSpinning}) => {
    if (isSpinning) {
        const spinnerStyle = {
            position: 'absolute',
            bottom: '50px',
            right: '50px'
        }

        return <CircularProgress
                    style={spinnerStyle}
                    size={200}
                    thickness={40}
                    color='red' />
    } else {
        return <span />
    }
}

ApiCallSpinner.propTypes = {
    isSpinning: PropTypes.bool.isRequired
}

export default ApiCallSpinner