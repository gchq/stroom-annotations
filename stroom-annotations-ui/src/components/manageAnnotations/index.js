import { connect } from 'react-redux'
import { withRouter } from 'react-router'

import ManageAnnotations from './manageAnnotations'

import {
    searchAnnotations,
    moreAnnotations
} from '../../actions/searchAnnotations'

export default connect(
    (state) => ({
        canRequestMore: state.manageAnnotations.canRequestMore,
        annotations: state.manageAnnotations.annotations,
        searchTerm: state.manageAnnotations.searchTerm
    }),
    {
        searchAnnotations,
        moreAnnotations
    }
)(withRouter(ManageAnnotations));