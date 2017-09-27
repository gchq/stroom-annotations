import React from 'react'
import { connect } from 'react-redux'

import CircularProgress from 'material-ui/CircularProgress';

export const PendingUpdatesSpinner = (props) => {
    if (props.isSpinning) {
        return <CircularProgress size={40} thickness={7} color='white' />
    } else {
        return <span />
    }
}

export default connect(
    (state) => ({
        isSpinning: (state.annotation.pendingUpdates > 0) || state.annotation.isFetching || state.annotations.isFetching,
    }),
    null
)(PendingUpdatesSpinner);
