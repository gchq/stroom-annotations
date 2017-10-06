import { connect } from 'react-redux'

import ErrorDisplay from './errorDisplay'

import { acknowledgeError } from '../../actions/acknowledgeApiMessages'

export default connect(
    (state) => ({
        errorMessages: state.errorMessages
    }),
    {
        acknowledgeError
    }
 )(ErrorDisplay);