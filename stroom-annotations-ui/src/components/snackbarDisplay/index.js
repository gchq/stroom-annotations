import { connect } from 'react-redux'

import SnackbarDisplay from './snackbarDisplay';

import { acknowledgeSnackbar } from '../../actions/acknowledgeApiMessages'

export default connect(
    (state) => ({
        messages: state.snackbarMessages
    }),
    {
        acknowledgeSnackbar
    }
 )(SnackbarDisplay);