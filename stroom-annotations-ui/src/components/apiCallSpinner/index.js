import { connect } from 'react-redux'

import ApiCallSpinner from './apiCallSpinner'

export default connect(
    (state) => ({
        isSpinning: (state.apiCalls.length > 0),
    }),
    null
)(ApiCallSpinner);
