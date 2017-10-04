import React from 'react'
import { connect } from 'react-redux'

import CircularProgress from 'material-ui/CircularProgress';

export const PendingUpdatesSpinner = (props) => {
    if (props.isSpinning) {
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

export default connect(
    (state) => ({
        isSpinning: (state.singleAnnotation.pendingUpdates > 0) || state.singleAnnotation.isFetching || state.manageAnnotations.isFetching || state.annotationHistory.isFetching,
    }),
    null
)(PendingUpdatesSpinner);
